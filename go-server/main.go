/* package main

import (
	"github.com/gin-gonic/gin"
	"net/http"
	"strconv"
	"google.golang.org/appengine"
)



type ResponseObject map[string]interface{}

func postStep(c *gin.Context) {
	ctx := appengine.NewContext(c.Request)
	uid := c.Param("Uid")
	day, _ := strconv.Atoi(c.Param("Day"))
	hour, _ := strconv.Atoi(c.Param("Hour"))
	count, _ := strconv.Atoi(c.Param("Count"))

	step := StepData{uid, day, hour, count}
	Create(ctx, &step)
	c.JSON(http.StatusOK, step)
}

func getDaySteps(c *gin.Context) {
	ctx := appengine.NewContext(c.Request)
	uid := c.Param("Uid")
	day, _ := strconv.Atoi(c.Param("Day"))

	total_count, _ := GetDaySteps(ctx, uid, day)
	c.JSON(http.StatusOK, total_count)
}

func getCurrentDaySteps(c *gin.Context) {
	ctx := appengine.NewContext(c.Request)
	uid := c.Param("Uid")

	total_count, _ := GetCurrentDaySteps(ctx, uid)
	c.JSON(http.StatusOK, total_count)
}

func getRangeDaysSteps(c *gin.Context) {
	ctx := appengine.NewContext(c.Request)
	uid := c.Param("Uid")
	startDay, _ := strconv.Atoi(c.Param("StartDay"))
	numDays, _ := strconv.Atoi(c.Param("NumDays"))

	totalCount, _ := GetRangeDaysSteps(ctx, uid, startDay, numDays)
	c.JSON(http.StatusOK, strconv.Itoa(totalCount))
}

func main() {

	router := gin.Default()
	router.GET("/single/:Uid/:Day", getDaySteps)
	router.GET("/current/:Uid", getCurrentDaySteps)
	router.GET("/range/:Uid/:StartDay/:NumDays", getRangeDaysSteps)
	router.GET("/", func(c *gin.Context) {
		c.JSON(200, gin.H{"message": "hello",
		})
	})
	router.POST("/:Uid/:Day/:Hour/:Count", postStep)

	http.Handle("/", router)
}


*/


package main

import (
	"log"
	"net/http"
	"os"
     	"strconv" 
	"github.com/gin-gonic/gin"
      "google.golang.org/appengine"
      "fmt"
	"google.golang.org/appengine/datastore"
	"google.golang.org/api/iterator"
	"golang.org/x/net/context"

)

type ResponseObject map[string]interface{}

const (
	index       = `StepData` // the table name
	invalidData = `error: invalid data`
	splitter    = `#`
)

//key is uid#day#hour
type StepData struct {
	Uid   string `json:"Uid"`
	Day   int    `json:"Day"`
	Hour  int    `json:"Hour"`
	Count int    `json:"Count"`
}


func postStep(c *gin.Context) {
	ctx := appengine.NewContext(c.Request)
	uid := c.Param("Uid")
	day, _ := strconv.Atoi(c.Param("Day"))
	hour, _ := strconv.Atoi(c.Param("Hour"))
	count, _ := strconv.Atoi(c.Param("Count"))

	step := StepData{uid, day, hour, count}
	Create(ctx, &step)
	c.JSON(http.StatusOK, step)
}

func Create(c context.Context, data *StepData) (*StepData, error) {
	if data == nil {
		return nil, fmt.Errorf(invalidData)
	}

	strId := stepToId(data)
	key := datastore.NewKey(c, index, strId, 0, nil)
	_, err := datastore.Put(c, key, data)
	return data, err
}

func GetDaySteps(c context.Context, uid string, day int) (int, error) {
	query := datastore.NewQuery(index).Filter("Uid =", uid).Filter("Day =", day)
	it := query.Run(c)
	totalCount := 0
	for {
		var stepData StepData
		_, err := it.Next(&stepData)
		if err == iterator.Done {
			//TODO: iterator.Done seems doesn't work
			return totalCount, nil
		}
		if err != nil {
			return totalCount, err
		}

		totalCount += stepData.Count
	}
}

func getCurrentDaySteps(c context.Context, uid string) (int, error) {
	query := datastore.NewQuery(index).Filter("Uid =", uid)
	it := query.Run(c)
	maxDay, stepCount := -1, 0

	for {
		var stepData StepData
		_, err := it.Next(&stepData)
		if err == iterator.Done {
			//TODO: iterator.Done seems doesn't work
			return stepCount, nil
		}
		if err != nil {
			return stepCount, err
		}

		if maxDay < stepData.Day {
			maxDay, stepCount = stepData.Day, stepData.Count
		} else if maxDay == stepData.Day {
			stepCount += stepData.Count
		}
	}

}

func getRangeDaysSteps(c context.Context, uid string, startDay int, numDays int) (int, error) {
	totalCount := 0
	for day := startDay; day < startDay+numDays; day++ {
		count, _ := GetDaySteps(c, uid, day)
		totalCount += count
	}
	return totalCount, nil
}

func dataToId(uid string, day int, hour int) string {
	return uid + splitter + strconv.Itoa(day) + strconv.Itoa(hour)
}

func stepToId(step *StepData) string {
	return dataToId(step.Uid, step.Day, step.Hour)
}




func main() {
	port := os.Getenv("PORT")

	if port == "" {
		port = "8080"
		log.Printf("Defaulting to port %s", port)
	}

	// Starts a new Gin instance with no middle-ware
	r := gin.New()

	// Define handlers
     // these 3 don't build for some reason? so commented out
    // r.GET("/single/:Uid/:Day", getDaySteps)
	//r.GET("/current/:Uid", getCurrentDaySteps)
//r.GET("/range/:Uid/:StartDay/:NumDays", getRangeDaysSteps)
 
	r.GET("/", func(c *gin.Context) {
		c.String(http.StatusOK, "Step Data test app")
	})
	r.GET("/ping", func(c *gin.Context) {
		c.String(http.StatusOK, "pong")
	})
     r.POST("/:Uid/:Day/:Hour/:Count", postStep)

	// Listen and serve on defined port
	log.Printf("Listening on port %s", port)
	r.Run(":" + port)
}

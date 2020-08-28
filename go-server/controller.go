package main

import (
	"fmt"
	"google.golang.org/appengine/datastore"
	"google.golang.org/api/iterator"
	"strconv"
	"golang.org/x/net/context"
)

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

func GetCurrentDaySteps(c context.Context, uid string) (int, error) {
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

func GetRangeDaysSteps(c context.Context, uid string, startDay int, numDays int) (int, error) {
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

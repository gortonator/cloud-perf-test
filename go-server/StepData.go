package main

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

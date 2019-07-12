package main

const (
	index       = `StepData` // the table name
	invalidData = `error: invalid data`
	splitter    = `#`
//	recentIndex    = `RecentDay`
)

//key is uid#day#hour
type StepData struct {
	Uid   string `json:"Uid"`
	Day   int    `json:"Day"`
	Hour  int    `json:"Hour"`
	Count int    `json:"Count"`
}

//type RecentDay struct {
//	Uid   string `json:"Uid"`
//    mostRecentDay int `json:"mostRecentDay"`
//}

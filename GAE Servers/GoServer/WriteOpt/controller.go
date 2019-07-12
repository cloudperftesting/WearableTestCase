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

func GetRecent(c context.Context, uid string) (int, error) {
    keyRecentDay := datastore.NewKey(c, recentIndex, uid, 0, nil)
    var recentDay RecentDay
    //fmt.Printf(">>>>>>>>>>>>>>>>before=%v", recentDay.mostRecentDay)
    err := datastore.Get(c, keyRecentDay, &recentDay)
    //fmt.Printf(">>>>>>>>>>>>>>>>after=%v", recentDay.mostRecentDay)
    //fmt.Println("Error------------>", err)
    if err == datastore.ErrNoSuchEntity{
        return -1, err
    }
    //fmt.Printf(">>>>>>>>>>>>>>>>=%v", recentDay.mostRecentDay)
    return recentDay.MostRecentDay, err
}

func CreateRecent(c context.Context, uid string, data *RecentDay)(*RecentDay, error){
    if data == nil {
        return nil, fmt.Errorf(invalidData)
    }
	key := datastore.NewKey(c, recentIndex, uid, 0, nil)
	_, err := datastore.Put(c, key, data)
    fmt.Println("Error------------>", err)

    //keyRecentDay := datastore.NewKey(c, recentIndex, uid, 0, nil)
    //var recentDay RecentDay
    //err = datastore.Get(c, keyRecentDay, &recentDay)
    //fmt.Printf(">>>>>>>>>>>>>>>>in create=%v", recentDay.mostRecentDay)

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
    recent, err := GetRecent(c, uid)
    //fmt.Printf(">>>>>>>>>>>>>>>>=%v", recent)

    if recent == -1 {
        return -1, err
    }
	query := datastore.NewQuery(index).Filter("Uid =", uid).Filter("Day =", recent)
	it := query.Run(c)
    totalCount := 0

	for {
		var stepData StepData
		_, err := it.Next(&stepData)
        //fmt.Printf(">>>>>>>>>>>>>>>>=%v", stepData.Count)
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

func GetRangeDaysSteps(c context.Context, uid string, startDay int, numDays int) (int, error) {
    recent, err := GetRecent(c, uid)
    if recent == -1 {
        return -1, err
    }
    if recent+1 > startDay+numDays {
        recent = startDay+numDays-1
    }

	totalCount := 0
	for day := startDay; day <= recent; day++ {
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

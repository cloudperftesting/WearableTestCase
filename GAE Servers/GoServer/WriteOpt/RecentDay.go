package main

const (
	recentIndex    = `RecentDay`
)

//key is uid
type RecentDay struct {
    MostRecentDay int `json:"Day"`
}

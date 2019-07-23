package main

import (
	"github.com/gin-gonic/gin"
	"net/http"
	"strconv"
	"google.golang.org/appengine"
    "fmt"
)

type ResponseObject map[string]interface{}

func postStep(c *gin.Context) {
	ctx := appengine.NewContext(c.Request)
	uid := c.Param("Uid")
	day, _ := strconv.Atoi(c.Param("Day"))
	hour, _ := strconv.Atoi(c.Param("Hour"))
	count, _ := strconv.Atoi(c.Param("Count"))
    if day < 0 || hour < 0 || hour > 23 || count < 0 || count > 5000 {
        c.JSON(http.StatusBadRequest, "not valid post")
    }

	step := StepData{uid, day, hour, count}
	Create(ctx, &step)
    
    recent, _ := GetRecent(ctx, uid)

    if recent < day {
        //fmt.Printf(">>>>>>>>recent is : %v", recent)
        //fmt.Printf(">>>>>>>>day is : %v", day)
        most_recent := RecentDay{day}
        //fmt.Printf(">>>>>>>>most recent day is : %v\n", most_recent.mostRecentDay)
        fmt.Printf("")
        CreateRecent(ctx, uid, &most_recent)
    }
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

    if total_count == -1 {
	    c.JSON(http.StatusOK, "no data yet")
        return
    }
	c.JSON(http.StatusOK, total_count)
}

func getRangeDaysSteps(c *gin.Context) {
	ctx := appengine.NewContext(c.Request)
	uid := c.Param("Uid")
	startDay, _ := strconv.Atoi(c.Param("StartDay"))
	numDays, _ := strconv.Atoi(c.Param("NumDays"))

	totalCount, _ := GetRangeDaysSteps(ctx, uid, startDay, numDays)
    if totalCount == -1 {
	    c.JSON(http.StatusBadRequest, "get range day fail")
        return
    }
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
    appengine.Main()
}

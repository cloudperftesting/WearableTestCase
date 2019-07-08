package main
import (
	//"context"
	//"encoding/json"
	//"errors"
	//"fmt"
	"reflect"
	//"io"
	//"log"
	"net/http"
	"os"
	//"path"
	"strconv"
	"google.golang.org/appengine"
	"google.golang.org/appengine/datastore"
	//"google.golang.org/api/iterator"
	//"cloud.google.com/go/pubsub"
	//"cloud.google.com/go/storage"

// 	uuid "github.com/gofrs/uuid"
 //	"github.com/gorilla/handlers"
 	"github.com/gin-gonic/gin"
// 	"github.com/GoogleCloudPlatform/golang-samples/getting-started/bookshelf"
 )
func main() {
	registerHandlers()
	appengine.Main()
}

type StepData struct{
	H0  int64    `json:"H0"`
	H1  int64    `json:"H1"`
	H2  int64    `json:"H2"`
	H3  int64    `json:"H3"`
	H4  int64    `json:"H4"`
	H5  int64    `json:"H5"`
	H6  int64    `json:"H6"`
	H7  int64    `json:"H7"`
	H8  int64    `json:"H8"`
	H9  int64    `json:"H9"`
	H10  int64    `json:"H10"`
	H11  int64    `json:"H11"`
	H12  int64    `json:"H12"`
	H13  int64    `json:"H13"`
	H14  int64    `json:"H14"`
	H15  int64    `json:"H15"`
	H16  int64    `json:"H16"`
	H17  int64    `json:"H17"`
	H18 int64    `json:"H18"`
	H19  int64    `json:"H19"`
	H20  int64    `json:"H20"`
	H21  int64    `json:"H21"`
	H22  int64    `json:"H22"`
	H23  int64    `json:"H23"`
	Total  int64 	  `json:"Count"`
}

type MostRecentDay struct{
	MRD	   int64    `json:"MostRecentDay"`
}

func registerHandlers(){
	//fmt.Println("started successfully")
	port := os.Getenv("PORT")
	if port == "" {
		port = "localhost:8080"
	}
	gin.SetMode(gin.ReleaseMode)
	router := gin.Default()
	//Make a subrouter for post methods
	router.GET("/single/:userId/:day/", getDayStepsHandler)
	router.GET("/current/:userId",getCurrentDayHandler)
	router.GET("/range/:userId/:startDay/:numDays/", getRangeDaysHandler)
	router.POST("/:userId/:day/:hour/:steps/", updateHandler)
	//router.POST("/delete", deleteHandler)
	// [START request_logging]
	// Delegate all of the HTTP routing and serving to the gorilla/mux router.
	// Log all requests using the standard Apache format.
	http.Handle("/", router)
	// [END request_logging]
}

func handle(w http.ResponseWriter, r *http.Request) {
	if r.URL.Path != "/" {
		http.NotFound(w, r)
		return
	}
	//fmt.Fprint(w, "Hello world!")
}

func updateHandler(c *gin.Context) {
	ctx := appengine.NewContext(c.Request)
	userId:= c.Param("userId")
	//fmt.Println(userId)
	day := c.Param("day")
	//fmt.Println(day)
	hour := c.Param("hour")
	//fmt.Println(hour)
	steps := c.Param("steps")
	//fmt.Println(steps)
	dayInt,_ := strconv.ParseInt(day,10,64)
	stepsInt,_ := strconv.ParseInt(steps,10,64)
	StepKeyString := userId +"#" +day
	hourString := "H" + hour
	//RecentDayKeyString := userId
	StepKey := datastore.NewKey(ctx,`StepData`,StepKeyString,0,nil)
	MSDKey := datastore.NewKey(ctx,`MostRecentDay`,userId,0,nil)
	//fmt.Println(StepKey)
	var stepRecord StepData
	//fmt.Println(stepRecord)
	err:=datastore.Get(ctx,StepKey,&stepRecord)
	if err == datastore.ErrNoSuchEntity{
		updateHour := reflect.ValueOf(&stepRecord).Elem().FieldByName(hourString)
		if  updateHour.IsValid(){
			//fmt.Println(updateHour)
			updateHour.SetInt(stepsInt)
		}
		stepRecord.Total = stepsInt
	}else{
		updateHour := reflect.ValueOf(&stepRecord).Elem().FieldByName(hourString)
		stepRecord.Total = stepRecord.Total+ stepsInt - updateHour.Int()
		//fmt.Println(stepRecord.Total)
		if  updateHour.IsValid(){
			//fmt.Println(updateHour)
			updateHour.SetInt(stepsInt)
		}
	}
	_,err = datastore.Put(ctx,StepKey,&stepRecord)
	//fmt.Println(stepRecord)
	var msdRecord MostRecentDay
	err = datastore.Get(ctx,MSDKey,&msdRecord)
	//fmt.Println(msdRecord)
	if err == datastore.ErrNoSuchEntity{
		msdRecord.MRD = dayInt
		_,err = datastore.Put(ctx,MSDKey,&msdRecord)
	}else if msdRecord.MRD<dayInt{
		msdRecord.MRD = dayInt
		_,err = datastore.Put(ctx,MSDKey,&msdRecord)
	}
	c.JSON(http.StatusOK,stepRecord)
	return
}

func getDayStepsHandler(c *gin.Context){
	ctx := appengine.NewContext(c.Request)
	userId:= c.Param("userId")
	day := c.Param("day")
	var stepRecord StepData
	StepKey := datastore.NewKey(ctx,`StepData`,userId+"#"+day,0,nil)
	err := datastore.Get(ctx,StepKey,&stepRecord)
	if err == datastore.ErrNoSuchEntity{
		MSDKey := datastore.NewKey(ctx,`MostRecentDay`,userId,0,nil)
		var msdRecord MostRecentDay
		err = datastore.Get(ctx,MSDKey,&msdRecord)
		if err == datastore.ErrNoSuchEntity{
			c.JSON(http.StatusOK,"no "+userId+" user")
			return
		}else{
			c.JSON(http.StatusOK,"no data for "+userId+" on day "+day)
			return
		}
	}else{
		c.JSON(http.StatusOK,stepRecord.Total)
	}
}

func getCurrentDayHandler(c *gin.Context){
	ctx := appengine.NewContext(c.Request)
	userId:= c.Param("userId")
	MSDKey := datastore.NewKey(ctx,`MostRecentDay`,userId,0,nil)
	var msdRecord MostRecentDay
	err := datastore.Get(ctx,MSDKey,&msdRecord)
	if err == datastore.ErrNoSuchEntity{
		c.JSON(http.StatusOK,"no data for "+userId+" user")
		return
	}else{
		StepKey := datastore.NewKey(ctx,`StepData`,userId+"#"+strconv.FormatInt(msdRecord.MRD,10),0,nil)
		var stepRecord StepData
		err = datastore.Get(ctx,StepKey,&stepRecord)
		c.JSON(http.StatusOK,stepRecord.Total)
		return
	}
}

func getRangeDaysHandler(c *gin.Context){
	ctx := appengine.NewContext(c.Request)
	userId:= c.Param("userId")
	startDay:=c.Param("startDay")
	startDayInt,_:=strconv.ParseInt(startDay,10,64)
	numDays:=c.Param("numDays")
	numDaysInt,_ :=strconv.ParseInt(numDays,10,64)
	MRDKey := datastore.NewKey(ctx,`MostRecentDay`,userId,0,nil)
	var mrdRecord MostRecentDay
	err := datastore.Get(ctx,MRDKey,&mrdRecord)
	if err == datastore.ErrNoSuchEntity{
		c.JSON(http.StatusOK,"no data for "+userId+" user")
		return
	}else{
		maxDay := mrdRecord.MRD+1
		if maxDay>startDayInt+numDaysInt{
			maxDay = startDayInt+numDaysInt
		}
		var totalCount int64
		var stepRecord StepData 
		//fmt.Println(startDay)
		//fmt.Println(maxDay)
		for i:= startDayInt; i<maxDay;i++{
			StepKey := datastore.NewKey(ctx,`StepData`,userId+"#"+strconv.FormatInt(i,10),0,nil)
			err = datastore.Get(ctx,StepKey,&stepRecord)
			totalCount += stepRecord.Total
		}
		c.JSON(http.StatusOK, totalCount)
		return 
	}

}

// func deleteHandler(c *gin.Context){
// 	query := datastore.NewQuery("").KeysOnly()
// 	keysIter := query.Run(c)
// 	var keys []*datastore.Key
// 	for{
// 		var key datastore.Key
// 		_,err := keysIter.Next(&key)
// 		if err == datastore.Done{
// 			break
// 		}
// 		if err != nil{
// 			fmt.Println(err)
// 			c.JSON(http.StatusOK, "error in iterator")
// 			return
// 		}
// 	}
// 	err := datastore.DeleteMulti(c,keys)
// 	if err == nil {
// 		c.JSON(http.StatusOK, "successfully deleted")
// 	}else{
// 		c.JSON(http.StatusOK, "error")
// 	}
// 	return
//}
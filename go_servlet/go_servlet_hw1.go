package main

import (
	"encoding/json"
	"fmt"
	"log"
	"net/http"
	"strconv"
	"strings"

	"github.com/gin-gonic/gin"
)

type AlbumsProfile struct {
	Artist string `json:"artist"`
	Title  string `json:"title"`
	Year   string `json:"year"`
}

func cleanData(data string) string {
	// Split the data into lines
	lines := strings.Split(data, "\n")
	profile := make(map[string]string)
	for _, line := range lines {
		line = strings.TrimSpace(line)
		if strings.HasPrefix(line, "artist:") {
			profile["artist"] = strings.Trim(line[len("artist:"):], " ")
		} else if strings.HasPrefix(line, "title:") {
			profile["title"] = strings.Trim(line[len("title:"):], " ")
		} else if strings.HasPrefix(line, "year:") {
			profile["year"] = strings.Trim(line[len("year:"):], " ")
		}
	}

	// Convert the map to a JSON string
	jsonOutput := fmt.Sprintf("{\"artist\": \"%s\", \"title\": \"%s\", \"year\": \"%s\"}", profile["artist"], profile["title"], profile["year"])
	return jsonOutput
}

func main() {
	r := gin.Default()

	r.POST("/albums", func(c *gin.Context) {
		file, errF := c.FormFile("image")

		profileSerialized := c.PostForm("profile")

		cleanProfileData := cleanData(profileSerialized)
		var profile AlbumsProfile
		errProfile := json.Unmarshal([]byte(cleanProfileData), &profile)

		if errProfile != nil {
			log.Print("Error parsing request data:", errProfile)
			c.JSON(http.StatusBadRequest, gin.H{"error": "Problem parsing the profile data: " + errProfile.Error()})
			return
		}

		artist := profile.Artist
		title := profile.Title
		year := profile.Year

		// Validation checks
		if errF != nil {
			log.Print("Problem getting the image:")
			c.JSON(http.StatusBadRequest, gin.H{"error": "Problem getting the image: " + errF.Error()})
			return
		}

		if file == nil {
			log.Print("Image file is required.")
			c.JSON(http.StatusBadRequest, gin.H{"error": "Image file is required."})
			return
		}
		if artist == "" || title == "" || year == "" {
			log.Print("Artist, title, and year are required.")
			c.JSON(http.StatusBadRequest, gin.H{"error": "Artist, title, and year are required fields."})
			return
		}

		// post stub: return the same data
		c.JSON(http.StatusOK, gin.H{
			"albumID":   "12345",
			"imageSize": file.Size,
		})
	})

	r.GET("/albums/:albumID", func(c *gin.Context) {
		albumID := c.Param("albumID")

		// Check for additional unexpected parameters
		if len(c.Params) > 1 {
			c.JSON(http.StatusBadRequest, gin.H{"error": "Unexpected parameters."})
			return
		}
		// Check if albumID is a valid integer
		_, err := strconv.Atoi(albumID)
		if err != nil {
			c.JSON(http.StatusBadRequest, gin.H{"error": "Invalid albumID format. Must be an integer."})
			return
		} else {
			c.JSON(http.StatusOK, gin.H{
				"artist": "Sex Pistols",
				"title":  "Never Mind The Bollocks!",
				"year":   "1977",
			})
		}
		/*
			// Existing logic
			if albumID == "12345" {
				c.JSON(http.StatusOK, gin.H{
					"artist": "Sex Pistols",
					"title":  "Never Mind The Bollocks!",
					"year":   "1977",
				})
			} else {
				c.JSON(http.StatusNotFound, gin.H{"msg": "Key not found"})
			}
		*/
	})

	r.Run(":8443")
}

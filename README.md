# FirebaseApp
This is a demo application in android to use Firebase. This app have google-services.json locally
while developing project.
But developer has not pushed this only file, since this repo is public.

#Setup firebase project and general best practices
https://firebase.google.com/docs/projects/learn-more?authuser=0

#Firebase authentication
https://firebase.google.com/docs/auth/?authuser=0
Basically email/password based authentication has done in this project.
https://firebase.google.com/docs/auth/android/password-auth?authuser=0

#Firebase database
You need to create database at firebase console and then set up rule
https://firebase.google.com/docs/auth/android/password-auth?authuser=0
https://firebase.google.com/docs/firestore/security/rules-structure?authuser=0


#structure of users and posts
{
  "posts" : { //main object
    "KWZjMFrrjjP6WKm8NTW1WjP0L8V2" : { //child as user-id same as user authentication, to separate each user posts
      "1566845962363" : { //another child as timestamp, an unique post-id (someone may use better uuid)
        "body" : "ikvff", //then actual post data
        "postId" : "1566845962363",
        "title" : "gdf"
      },
      "1566846129700" : {
        "body" : "jhgg",
        "postId" : "1566846129700",
        "title" : "hgg"
      }
    }
  },
  "users" : { //main object as user profile
    "Bk7EbTyDoUYTQbttmgqQw3sATE73" : { //child as user-id same as user authentication, to separate each user posts
      "age" : 25, //then actual profile data
      "mobile" : "123455678",
      "name" : "Amit Nagar"
    },
    "CmQDyWYbjcQMdBJD08bgYscBPG63" : {
      "age" : 21,
      "mobile" : "vhjj",
      "name" : "ashugv"
    },
    "Nmrdzhbj4KQyA2Ojw5ts0qTk6Ik2" : {
      "age" : 31,
      "mobile" : "9990874778",
      "name" : "Ashutosh"
    }
  }
}
# REST Scheduler API Reader for Android

An Android app that utilizes the Rest Scheduler API

- Gets a list of employees and all shifts without an API key (for testing)
- Gets shifts, co-workers, weekly hours, and the manager for each shift for employees
- Does not implement the full API (no functionality for managers) 

For a description of the full API see: https://github.com/brifishjones/rest-scheduler

## API Key

The API Key would normally be a long GUID such as `3b298e650c17ac77f140542bfc722b01` 
but for ease of testing it will be of the form `id:name`. Examples are:

- 4:James Young
- 5:Samantha Brown
- 6:Josh Rollins

Note that users 1, 2, and 3 are managers not employees. If you use an API key for a manager
an error will be returned.

## API subset for employees

The corresponding curl command will be replaced by a Java HttpsURLConnection call in a separate
AsyncClass thread. The API key will be sent in the header.

As an employee, I want to know when I am working, by being able to see all of the shifts assigned to me.<br>
GET /users/123/shifts<br>
`curl -i -H "authorization: 4:James Young" -w "\n" https://gentle-brushlands-1205.herokuapp.com/users/4/shifts` 

As an employee, I want to know whom I am working with, by being able see the employees that are working during the same time period as me.<br>
Returns future shifts<br>
GET /co-workers/123<br>
`curl -i -H "authorization: 5:Samantha Brown" -w "\n" https://gentle-brushlands-1205.herokuapp.com/co-workers/5`

As an employee, I want to know how much I worked, by being able to get a summary of hours worked for each week.<br>
Week starts at midnight on Monday<br>
Break hours are not substracted from total<br>
GET /weekly-hours/123<br>
`curl -i -H "authorization: 6:Josh Rollins" -w "\n" https://gentle-brushlands-1205.herokuapp.com/weekly-hours/6`

As an employee, I want to be able to contact my managers, by seeing manager contact information for my shifts.<br>
GET managers/123<br>
`curl -i -H "authorization: 7:Sophia Sanders" -w "\n" https://gentle-brushlands-1205.herokuapp.com/managers/7`

For testing only. Not part of the API

GET /users<br>
`curl -i -w "\n" https://gentle-brushlands-1205.herokuapp.com/users`<br>
`curl -i -H "Accept: text/xml" https://gentle-brushlands-1205.herokuapp.com/users`<br>
`curl -i -H "Accept: text/x-yaml" https://gentle-brushlands-1205.herokuapp.com/users`

GET /shifts<br>
`curl -i -w "\n" https://gentle-brushlands-1205.herokuapp.com/shifts`

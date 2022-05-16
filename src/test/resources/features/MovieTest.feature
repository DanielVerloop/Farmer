@MovieTest
Feature: Ticket Booking Tests 

  Scenario Outline: Setup Movie
	  Given I want to setup a new movie
	  When the movie has the name <name>
	  And the movie has a rating of <rating>
	  And the duration of the movie is <duration>
	  And the cast of the movie is <mainCast>
	  Then I validate that the the new movie is setup correctly.

	  Examples:
		  | name 			| rating | duration 		| mainCast 					|
		  | Star Wars		| PG-13	 | 2hrs 20 min	| Daisy Ridley, John Boyega 	|
	
  	
#VesselBuilder

Current Issues:
1. Edge-thinning produces unwanted tail vertices at the end of the edge.
	- Can fix by omitting trace points at the end that did not find any white pixels when producing the output vertex array.
2. Edge tracing still produces some jagged bits when the edge is not very clean.
	- Can try to fix by changing white pixel detection to conform more to the forward vector.
	- OR Can try to fix by adding more detailed cleaning tools to let the user fix this problem.
3. Preview pictures still don't display until the second button-press on the view page.
4. The view page allows the highlighting of multiple files even though only one is selected at a time.
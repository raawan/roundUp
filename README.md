USAGE:

1) update application.properties with the values of access token and root-context of API
2) From the project folder run -> 
        mvn clean install
3) start application using 
    java -jar target/<name-of-jar-file>
4) use Postman or curl {assuming the application started on 8080}
    
    http://localhost:8080/accounts/<accountId>/savingsgoal?triggerRoundUp=true
    
    Content-type: application/json
    
NOTE: please create savingsGoal and some transaction feed if you are using sandbox model
    
ASSUMPTION:
1) Every request will trigger a roundUp for the week. It will not consider that the feed has already been a part of earlier roundup
2) The problem statment mentioned to trigger a roundup for a week. Its not clear what week is - so I assumed a week starts from Monday
3) I assumed the roundUp sum is taken from the users savings or balance. I couldnt found an API to make update to the savings after a roundUp is triggered


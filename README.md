# Choo Choo Mail Control System

Built on adopt java jdk 11 with gradle

Main class in src/main/java/Main.class

### Sample input used

### Sample 1
3  
A  
B  
C  
2  
E1,A,B,3  
E2,B,C,1  
1  
P1,A,C,5  
1  
Q1,B,6  

@2, n = B, q = Q1, load= [P1], drop= [], moving B->A:E1 arr 2  
@5, n = B, q = Q1, load= [], drop= [], moving B->A:E1 arr 6  
@6, n = B, q = Q1, load= [], drop= [P1], moving B->C:E2 arr 6   

### Sample 2
5  
A  
B  
C  
D  
E  
5  
E1,A,B,4  
E2,B,C,1  
E3,B,D,1  
E4,D,E,1  
E5,E,A,1  
1  
P1,A,C,5  
1  
Q1,B,6  

@0, n = B, q = Q1, load= [], drop= [], moving B->D:E3 arr 1  
@1, n = D, q = Q1, load= [], drop= [], moving D->E:E4 arr 2  
@2, n = E, q = Q1, load= [P1], drop= [], moving E->A:E5 arr 2  
@3, n = E, q = Q1, load= [], drop= [], moving E->A:E5 arr 4  
@4, n = E, q = Q1, load= [], drop= [], moving E->D:E4 arr 5  
@5, n = D, q = Q1, load= [], drop= [], moving D->B:E3 arr 6  
@6, n = B, q = Q1, load= [], drop= [P1], moving B->C:E2 arr 6  

### Notes
There are two copies of code being developed.
This is a simplified version that was developed as a more optimised version is still not working as expected.
This version, at any single time, moves only one package and one train. However,  the train will always be moving in its optimised route to the destination.


### For Improvements
There are more things that can be improved in this version. Some of the stuff includes the following which unfortunately the optimised branch of the code tried to achieve:  

Codewise:  

Code should follow closely to how a realistic package transfer for train as some object should not have information of other object
Variables naming can be better
Code can be more defensive (e.g checking of optional)
Test cases to be added

Featurewise:
1. fixing of output display (timing is still in accurate)
2. allow multiple train to run at a same time
3. allow maximum packages to be picked up till train is full
4. selecting train to that is closest to station to pickup delivery
5. all routes being shared across all train to optimise and used as mesh network
6. allowing train to dropoff package midway if another train happens to be in the station onroute to the other path


A more optimised version can be found in the main branch: 
https://github.com/JasonSia/choochoo

### Credits
Using dijkstra algorithm from: https://www.baeldung.com/java-dijkstra
Using knapsack algorithm from: https://www.baeldung.com/java-knapsack


expresss-orm
============

Object Relational Mapping Framework

1. DB connection
2. *setup table-class mapping-rule(like Rails, User to Users) --key step
3. *convert save() or findById() to SQL  --key step
4. convention over configration(like Rails, without setter and getter)
5. add primitive type mapping(int, long, String)
6. *add one to many relationship mapping  --key step
7. test for N+1 issue


how to using instrument lib
---------------------------
0. `gradle clean idea`
1. in instrument folder, execute `gradle jar`
2. setup configuration so we can using the instrument agent jar, when running junit test.
In IntelliJ IDEA,  Run-->Edit Configurations…-->Defaults-->JUnit:
set "VM Options:" to **-javaagent:./instrument/build/libs/instrument.jar**

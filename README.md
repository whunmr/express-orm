expresss-orm
============

Object Relational Mapping Framework

1. DB connection in singleton mode(Registry)
2. *setup table-class mapping-rule(like Rails, User to Users) --key step
3. *convert save() or findById() to SQL  --key step
4. convention over configration(like Rails, without setter and getter)
5. add primitive type mapping(int, long, String)
6. *add one to many relationship mapping  --key step
7. test for N+1 issue

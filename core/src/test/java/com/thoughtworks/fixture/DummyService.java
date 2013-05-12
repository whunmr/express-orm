package com.thoughtworks.fixture;


import com.thoughtworks.annotation.Transactional;

public class DummyService {

    @Transactional
    public void saveUserInTransactionAndExceptionOccurs(User user) {
        user.save();
        throw new IllegalStateException("Can not connect to remote service, need rollback");
    }

    @Transactional
    public void saveUserInTransactionAndNoExceptionOccurs(User user) {
        user.save();
    }
}



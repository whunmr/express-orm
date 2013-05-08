package com.thoughtworks.fixture;

import com.thoughtworks.Transactional;

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



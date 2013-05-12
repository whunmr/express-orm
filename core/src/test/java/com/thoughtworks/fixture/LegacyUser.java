package com.thoughtworks.fixture;

import com.thoughtworks.Model;
import com.thoughtworks.annotation.Column;
import com.thoughtworks.annotation.Table;

@Table("users")
public class LegacyUser extends Model {
    @Column("first_name")
    public String __firstName;
    public String email;
    public Article[] articles;

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        User user = (User) o;

        if (email != null ? !email.equals(user.email) : user.email != null) return false;
        if (__firstName != null ? !__firstName.equals(user.firstName) : user.firstName != null) return false;

        return true;
    }

    @Override
    public int hashCode() {
        int result = __firstName != null ? __firstName.hashCode() : 0;
        result = 31 * result + (email != null ? email.hashCode() : 0);
        return result;
    }
}

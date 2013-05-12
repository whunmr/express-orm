package com.thoughtworks.fixture;

import com.thoughtworks.Model;
import com.thoughtworks.annotation.Column;
import com.thoughtworks.annotation.Table;

import java.util.Arrays;

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

        LegacyUser that = (LegacyUser) o;

        if (__firstName != null ? !__firstName.equals(that.__firstName) : that.__firstName != null) return false;
        if (!Arrays.equals(articles, that.articles)) return false;
        if (email != null ? !email.equals(that.email) : that.email != null) return false;

        return true;
    }

    @Override
    public int hashCode() {
        int result = __firstName != null ? __firstName.hashCode() : 0;
        result = 31 * result + (email != null ? email.hashCode() : 0);
        result = 31 * result + (articles != null ? Arrays.hashCode(articles) : 0);
        return result;
    }
}

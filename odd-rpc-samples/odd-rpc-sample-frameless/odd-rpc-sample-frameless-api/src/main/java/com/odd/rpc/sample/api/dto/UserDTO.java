package com.odd.rpc.sample.api.dto;

import java.io.Serializable;

/**
 * @author oddity
 * @create 2023-12-03 15:22
 */
public class UserDTO implements Serializable {

    private static final long serialVersionUID = 42L;

    private String name;
    private String word;

    public UserDTO() {
    }
    public UserDTO(String name, String word) {
        this.name = name;
        this.word = word;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getWord() {
        return word;
    }

    public void setWord(String word) {
        this.word = word;
    }

    @Override
    public String toString() {
        return "UserDTO{" +
                "name='" + name + '\'' +
                ", word='" + word + '\'' +
                '}';
    }
}

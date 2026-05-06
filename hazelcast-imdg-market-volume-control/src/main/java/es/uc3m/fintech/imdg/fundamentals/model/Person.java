package es.uc3m.fintech.imdg.fundamentals.model;

import java.io.Serializable;

/**
 * Plain serializable POJO used to demonstrate that arbitrary Java objects can
 * traverse the cluster: Hazelcast serializes instances to bytes when storing
 * them in a distributed {@code IMap} and rehydrates them on the reading node.
 */
public class Person implements Serializable {

    private static final long serialVersionUID = 1L;

    private String name;
    private Integer zipCode;
    private String streetName;
    private String fullAddress;

    public Person(String name, Integer zipCode, String streetName, String fullAddress) {
        this.name = name;
        this.zipCode = zipCode;
        this.streetName = streetName;
        this.fullAddress = fullAddress;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Integer getZipCode() {
        return zipCode;
    }

    public void setZipCode(Integer zipCode) {
        this.zipCode = zipCode;
    }

    public String getStreetName() {
        return streetName;
    }

    public void setStreetName(String streetName) {
        this.streetName = streetName;
    }

    public String getFullAddress() {
        return fullAddress;
    }

    public void setFullAddress(String fullAddress) {
        this.fullAddress = fullAddress;
    }

    @Override
    public String toString() {
        return "Person{" +
                "name='" + name + '\'' +
                ", zipCode=" + zipCode +
                ", streetName='" + streetName + '\'' +
                ", fullAddress='" + fullAddress + '\'' +
                '}';
    }
}

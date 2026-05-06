package com.uc3m.pu.model;

//pongo record y no class para que me cree automaticamente el constructor y los getter
//para leer los datos
public record Message(long id, String msg) {
}
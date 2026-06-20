package com.example;

import com.example.network.Connection;

public class NetworkPackage <T> {
    private final T data;
    private final Connection connection;

    public NetworkPackage(T data, Connection connection) {
        this.data = data;
        this.connection = connection;
    }

    public T getData() {
        return data;
    }

    public Connection getConnection() {
        return connection;
    }
}

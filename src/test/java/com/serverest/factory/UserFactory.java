package com.serverest.factory;

import com.serverest.model.User;
import com.serverest.utils.DataFactory;

public class UserFactory {
    public static User createAdmin() {
        return DataFactory.createAdminUser();
    }

    public static User createCommon() {
        return DataFactory.createDefaultUser();
    }
}
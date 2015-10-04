package com.shopmy.shopmy.service;

import com.shopmy.shopmy.model.ShopInfo;

import java.util.Map;

/**
 * Created by stepan on 4. 10. 2015.
 */
public class ShopInfoService {
    private ShopInfoService instance;
    private Map<String, ShopInfo> shopInfoMap;

    /**
     * Private constructor
     */
    private ShopInfoService(){

    }

    public ShopInfoService getInstance(){
        if (instance == null){
            instance = new ShopInfoService();
        }
        return instance;
    }

}

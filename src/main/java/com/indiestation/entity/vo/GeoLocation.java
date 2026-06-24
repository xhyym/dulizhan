package com.indiestation.entity.vo;

import lombok.Data;

/**
 * IP 地理位置信息
 *
 * @author IndieStation
 */
@Data
public class GeoLocation {

    /** 国家 */
    private String country;

    /** 省/州 */
    private String province;

    /** 城市 */
    private String city;

    public GeoLocation() {}

    public GeoLocation(String country, String province, String city) {
        this.country = country;
        this.province = province;
        this.city = city;
    }

    public static GeoLocation unknown() {
        return new GeoLocation("Unknown", "", "");
    }
}

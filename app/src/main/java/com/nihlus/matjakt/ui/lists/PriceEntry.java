package com.nihlus.matjakt.ui.lists;

import com.nihlus.matjakt.constants.Constants;

import java.util.HashMap;

/**
 * TODO: Deprecate class in favor of MatjaktPrice
 * Represents an entry in the visible price list
 */
@SuppressWarnings({"HardCodedStringLiteral", "WeakerAccess"})
public class PriceEntry
{
    public String Chain;
    public String Extra;
    public double Latitude;
    public double Longitude;
    public String Location;
    public double Price;
    public String Timestamp;

    private boolean isSpecial;

    public static PriceEntry getAddEntry()
    {
        PriceEntry entry = new PriceEntry();
        entry.Chain = "+";
        entry.Extra = "";
        entry.Latitude = -1;
        entry.Longitude = -1;
        entry.Location = "";
        entry.Price = 0;
        entry.Timestamp = "0";

        entry.isSpecial = true;

        return entry;
    }

    public HashMap<String, String> getHashMap()
    {
        HashMap<String, String> hashMap = new HashMap<>();
        hashMap.put(Constants.PRICEMAPID_STORE, Chain);

        //if the entry is an "Add" entry (i.e, just a plus sign) or Loading, don't add the price or the comma
        if (isSpecial)
        {
            hashMap.put(Constants.PRICEMAPID_OFFER, "");
            hashMap.put(Constants.PRICEMAPID_PRICE, "");
        }
        else
        {
            hashMap.put(Constants.PRICEMAPID_OFFER, Extra + ", " + Location);
            hashMap.put(Constants.PRICEMAPID_PRICE, Double.toString(Price));
        }

        hashMap.put(Constants.PRICEMAPID_LAT, String.valueOf(Latitude));
        hashMap.put(Constants.PRICEMAPID_LON, String.valueOf(Longitude));
        hashMap.put(Constants.PRICEMAPID_LOC, Location);
        hashMap.put(Constants.PRICEMAPID_TIMESTAMP, Timestamp);

        return hashMap;
    }
}

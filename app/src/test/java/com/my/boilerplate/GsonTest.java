package com.my.boilerplate;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.my.boilerplate.data.StickerBundle.BundleItem;
import com.my.boilerplate.util.ListUtil;

import junit.framework.Assert;

import org.junit.Test;

import java.lang.reflect.Array;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;

import static org.junit.Assert.assertEquals;

public class GsonTest {

    @Test
    public void serializeAndDeserializeList() throws Exception {
        final List<BundleItem> orgItems = new ArrayList<>();
        // Add items.
        orgItems.add(new BundleItem("http://1", "/a/b/c/t/1", "/a/b/c/1"));
        orgItems.add(new BundleItem("http://2", "/a/b/c/t/2", "/a/b/c/2"));
        orgItems.add(new BundleItem("http://3", "/a/b/c/t/3", "/a/b/c/3"));

        // Serialize the list.
        final String json = ListUtil.toJson(orgItems, BundleItem.class);

        System.out.print("serialized json(" + orgItems.size() + ")=" + json);

        // Deserialize the list.
        final List<BundleItem> newItems = ListUtil.fromJson(json, BundleItem.class);
        Assert.assertTrue(newItems.size() == orgItems.size());
        for (int i = 0; i < newItems.size(); ++i) {
            Assert.assertTrue(newItems.get(i).equals(orgItems.get(i)));
        }
    }
}

/*
 * Copyright 2018 Nordstrom, Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.nordstrom.xrpc;

import static org.junit.jupiter.api.Assertions.*;

import com.google.common.collect.ImmutableMap;
import com.google.common.hash.Funnels;
import io.netty.util.internal.PlatformDependent;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Random;
import org.junit.jupiter.api.Test;

class RendezvousHashTest {
  @Test
  public void get() throws Exception {
    List<String> nodeList = new ArrayList<>();
    Map<String, List<String>> mm = PlatformDependent.newConcurrentHashMap();
    for (int i = 0; i < 100; i++) {
      nodeList.add(("Host" + i));
      mm.put(("Host" + i), new ArrayList<>());
    }

    RendezvousHash<CharSequence> rendezvousHash =
        new RendezvousHash<>(Funnels.stringFunnel(Charset.defaultCharset()), nodeList);
    Random r = new Random();
    for (int i = 0; i < 100000; i++) {
      String thing = (Integer.toString(r.nextInt(123456789)));
      List<CharSequence> hosts = rendezvousHash.get(thing.getBytes(), 3);
      hosts.forEach(
          xs -> {
            mm.get(xs).add(thing);
          });
    }

    List<Integer> xx = new ArrayList<>();
    mm.keySet()
        .forEach(
            xs -> {
              xx.add(mm.get(xs).size());
            });

    Double xd = xx.stream().mapToInt(x -> x).average().orElse(-1);
    assertEquals(3000, xd.intValue());
  }

  @Test
  void simpleGet() {
    Map<String, String> map =
        new ImmutableMap.Builder<String, String>()
            .put("a", "1")
            .put("b", "2")
            .put("c", "3")
            .put("d", "4")
            .put("e", "5")
            .build();
    RendezvousHash<CharSequence> hasher =
        new RendezvousHash<>(Funnels.stringFunnel(XrpcConstants.DEFAULT_CHARSET), map.keySet());
    String k1 = "foo";
    String k2 = "bar";
    String k3 = "baz";
    String k4 = "biz";

    assertEquals(hasher.getOne(k1.getBytes()), hasher.getOne(k1.getBytes()));
    assertEquals(hasher.getOne(k2.getBytes()), hasher.getOne(k2.getBytes()));
    assertEquals(hasher.getOne(k3.getBytes()), hasher.getOne(k3.getBytes()));
    assertEquals(hasher.getOne(k4.getBytes()), hasher.getOne(k4.getBytes()));

    System.out.println(hasher.getOne(k1.getBytes()));
    System.out.println(hasher.getOne(k2.getBytes()));
    System.out.println(hasher.getOne(k3.getBytes()));
    System.out.println(hasher.getOne(k4.getBytes()));

    System.out.println(hasher.getOne(k1.getBytes()));
    System.out.println(hasher.getOne(k2.getBytes()));
    System.out.println(hasher.getOne(k3.getBytes()));
    System.out.println(hasher.getOne(k4.getBytes()));

    assertNotEquals(hasher.getOne(k1.getBytes()), hasher.getOne(k4.getBytes()));
  }
}

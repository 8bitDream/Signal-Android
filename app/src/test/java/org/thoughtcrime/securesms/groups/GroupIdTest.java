package org.thoughtcrime.securesms.groups;

import org.junit.Test;
import org.signal.zkgroup.InvalidInputException;
import org.signal.zkgroup.groups.GroupIdentifier;
import org.signal.zkgroup.groups.GroupMasterKey;
import org.thoughtcrime.securesms.util.Hex;

import java.io.IOException;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertNotSame;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertTrue;
import static org.thoughtcrime.securesms.groups.ZkGroupLibraryUtil.assumeZkGroupSupportedOnOS;

public final class GroupIdTest {

  @Test
  public void can_create_for_gv1() {
    GroupId.V1 groupId = GroupId.v1(new byte[]{ 0, 1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12, 13, 14, 15 });

    assertEquals("__textsecure_group__!000102030405060708090a0b0c0d0e0f", groupId.toString());
    assertFalse(groupId.isMms());
  }

  @Test
  public void can_parse_gv1() {
    GroupId groupId = GroupId.parse("__textsecure_group__!000102030405060708090a0b0c0d0e0f");

    assertEquals("__textsecure_group__!000102030405060708090a0b0c0d0e0f", groupId.toString());
    assertArrayEquals(new byte[]{ 0, 1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12, 13, 14, 15 }, groupId.getDecodedId());
    assertFalse(groupId.isMms());
    assertTrue(groupId.isV1());
    assertFalse(groupId.isV2());
    assertTrue(groupId.isPush());
  }

  @Test
  public void can_create_for_gv2_from_GroupIdentifier() throws IOException, InvalidInputException {
    GroupId.V2 groupId = GroupId.v2(new GroupIdentifier(Hex.fromStringCondensed("0123456789abcdef0123456789abcdef0123456789abcdef0123456789abcdef")));

    assertEquals("__textsecure_group__!0123456789abcdef0123456789abcdef0123456789abcdef0123456789abcdef", groupId.toString());
    assertFalse(groupId.isMms());
    assertFalse(groupId.isV1());
    assertTrue(groupId.isV2());
    assertTrue(groupId.isPush());
  }

  @Test
  public void can_create_for_gv2_from_GroupMasterKey() throws IOException, InvalidInputException {
    assumeZkGroupSupportedOnOS();

    GroupId.V2 groupId = GroupId.v2(new GroupMasterKey(Hex.fromStringCondensed("0123456789abcdef0123456789abcdef0123456789abcdef0123456789abcdef")));

    assertEquals("__textsecure_group__!9f475f59b2518bff6df22e820803f0e3585bd99e686fa7e7fbfc2f92fd5d953e", groupId.toString());
    assertFalse(groupId.isMms());
    assertFalse(groupId.isV1());
    assertTrue(groupId.isV2());
    assertTrue(groupId.isPush());
  }

  @Test
  public void can_parse_gv2() throws IOException {
    GroupId groupId = GroupId.parse("__textsecure_group__!9f475f59b2518bff6df22e820803f0e3585bd99e686fa7e7fbfc2f92fd5d953e");

    assertEquals("__textsecure_group__!9f475f59b2518bff6df22e820803f0e3585bd99e686fa7e7fbfc2f92fd5d953e", groupId.toString());
    assertArrayEquals(Hex.fromStringCondensed("9f475f59b2518bff6df22e820803f0e3585bd99e686fa7e7fbfc2f92fd5d953e"), groupId.getDecodedId());
    assertFalse(groupId.isMms());
    assertFalse(groupId.isV1());
    assertTrue(groupId.isV2());
    assertTrue(groupId.isPush());
  }

  @Test
  public void can_create_for_mms() {
    GroupId.Mms groupId = GroupId.mms(new byte[]{ 0, 1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12, 13, 14, 15 });

    assertEquals("__signal_mms_group__!000102030405060708090a0b0c0d0e0f", groupId.toString());
    assertTrue(groupId.isMms());
    assertFalse(groupId.isV1());
    assertFalse(groupId.isV2());
    assertFalse(groupId.isPush());
  }

  @Test
  public void can_parse_mms() {
    GroupId groupId = GroupId.parse("__signal_mms_group__!000102030405060708090a0b0c0d0e0f");

    assertEquals("__signal_mms_group__!000102030405060708090a0b0c0d0e0f", groupId.toString());
    assertArrayEquals(new byte[]{ 0, 1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12, 13, 14, 15 }, groupId.getDecodedId());
    assertTrue(groupId.isMms());
    assertFalse(groupId.isV1());
    assertFalse(groupId.isV2());
    assertFalse(groupId.isPush());
  }

  @SuppressWarnings("ConstantConditions")
  @Test
  public void can_parse_null() {
    GroupId groupId = GroupId.parseNullable(null);

    assertNull(groupId);
  }
  
  @Test
  public void can_parse_gv1_with_parseNullable() {
    GroupId groupId = GroupId.parseNullable("__textsecure_group__!000102030405060708090a0b0c0d0e0f");

    assertEquals("__textsecure_group__!000102030405060708090a0b0c0d0e0f", groupId.toString());
    assertArrayEquals(new byte[]{ 0, 1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12, 13, 14, 15 }, groupId.getDecodedId());
    assertFalse(groupId.isMms());
    assertTrue(groupId.isV1());
    assertFalse(groupId.isV2());
    assertTrue(groupId.isPush());
  }
  
  @Test(expected = AssertionError.class)
  public void bad_encoding__bad_prefix__parseNullable() {
    GroupId.parseNullable("__BAD_PREFIX__!000102030405060708090a0b0c0d0e0f");
  }

  @Test(expected = AssertionError.class)
  public void bad_encoding__empty__parseNullable() {
    GroupId.parseNullable("");
  }
  
  @Test(expected = AssertionError.class)
  public void bad_encoding__odd_hex__parseNullable() {
    GroupId.parseNullable("__textsecure_group__!0001020305060708090bODD_HEX");
  }

  @Test(expected = AssertionError.class)
  public void bad_encoding__bad_prefix__parse() {
    GroupId.parse("__BAD_PREFIX__!000102030405060708090a0b0c0d0e0f");
  }
  
  @Test(expected = AssertionError.class)
  public void bad_encoding__odd_hex__parse() {
    GroupId.parse("__textsecure_group__!0001020305060708090b0c0d0e0fODD_HEX");
  }

  @Test
  public void get_bytes() {
    byte[] bytes = { 0, 1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12, 13, 14, 15 };
    GroupId groupId = GroupId.v1(bytes);

    assertArrayEquals(bytes, groupId.getDecodedId());
  }

  @Test
  public void equality() {
    GroupId groupId1 = GroupId.v1(new byte[]{ 0, 1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12, 13, 14, 15 });
    GroupId groupId2 = GroupId.v1(new byte[]{ 0, 1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12, 13, 14, 15 });

    assertNotSame(groupId1, groupId2);
    assertEquals(groupId1, groupId2);
    assertEquals(groupId1.hashCode(), groupId2.hashCode());
  }

  @Test
  public void inequality_by_bytes() {
    GroupId groupId1 = GroupId.v1(new byte[]{ 0, 1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12, 13, 14, 15 });
    GroupId groupId2 = GroupId.v1(new byte[]{ 0, 3, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12, 13, 14, 15 });

    assertNotSame(groupId1, groupId2);
    assertNotEquals(groupId1, groupId2);
    assertNotEquals(groupId1.hashCode(), groupId2.hashCode());
  }

  @Test
  public void inequality_of_sms_and_mms() {
    GroupId groupId1 = GroupId.v1(new byte[]{ 0, 1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12, 13, 14, 15 });
    GroupId groupId2 = GroupId.mms(new byte[]{ 0, 1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12, 13, 14, 15 });

    assertNotSame(groupId1, groupId2);
    assertNotEquals(groupId1, groupId2);
    assertNotEquals(groupId1.hashCode(), groupId2.hashCode());
  }

  @Test
  public void inequality_with_null() {
    GroupId groupId = GroupId.v1(new byte[]{ 0, 1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12, 13, 14, 15 });

    assertNotEquals(groupId, null);
  }

  @Test
  public void require_mms() {
    GroupId groupId = GroupId.parse("__signal_mms_group__!000102030405060708090a0b0c0d0e0f");

    GroupId.Mms mms = groupId.requireMms();

    assertSame(groupId, mms);
  }

  @Test
  public void require_v1_and_push() {
    GroupId groupId = GroupId.parse("__textsecure_group__!000102030405060708090a0b0c0d0e0f");

    GroupId.V1   v1   = groupId.requireV1();
    GroupId.Push push = groupId.requirePush();

    assertSame(groupId, v1);
    assertSame(groupId, push);
  }

  @Test
  public void require_v2_and_push() {
    GroupId groupId = GroupId.parse("__textsecure_group__!9f475f59b2518bff6df22e820803f0e3585bd99e686fa7e7fbfc2f92fd5d953e");

    GroupId.V2   v2   = groupId.requireV2  ();
    GroupId.Push push = groupId.requirePush();

    assertSame(groupId, v2);
    assertSame(groupId, push);
  }

  @Test(expected = AssertionError.class)
  public void cannot_require_push_of_mms() {
    GroupId groupId = GroupId.parse("__signal_mms_group__!000102030405060708090a0b0c0d0e0f");

    groupId.requirePush();
  }

  @Test(expected = AssertionError.class)
  public void cannot_require_v1_of_mms() {
    GroupId groupId = GroupId.parse("__signal_mms_group__!000102030405060708090a0b0c0d0e0f");

    groupId.requireV1();
  }

  @Test(expected = AssertionError.class)
  public void cannot_require_v2_of_mms() {
    GroupId groupId = GroupId.parse("__signal_mms_group__!000102030405060708090a0b0c0d0e0f");

    groupId.requireV2();
  }

  @Test(expected = AssertionError.class)
  public void cannot_require_v1_of_v2() {
    GroupId groupId = GroupId.parse("__textsecure_group__!9f475f59b2518bff6df22e820803f0e3585bd99e686fa7e7fbfc2f92fd5d953e");

    groupId.requireV1();
  }

  @Test(expected = AssertionError.class)
  public void cannot_require_v2_of_v1() {
    GroupId groupId = GroupId.parse("__textsecure_group__!000102030405060708090a0b0c0d0e0f");

    groupId.requireV2();
  }

  @Test(expected = AssertionError.class)
  public void cannot_create_v1_with_a_v2_length() throws IOException {
    GroupId.v1(Hex.fromStringCondensed("9f475f59b2518bff6df22e820803f0e3585bd99e686fa7e7fbfc2f92fd5d953e"));
  }

  @Test(expected = AssertionError.class)
  public void cannot_create_v2_with_a_v1_length() throws IOException {
    GroupId.v2(Hex.fromStringCondensed("000102030405060708090a0b0c0d0e0f"));
  }
}

package com.example.txl;

import java.util.ArrayList;
import java.util.List;

public class Contact {
  private long id;
  private String name;
  private List<String> phones;
  private String note;
  private String sortLetter;
  private String avatarPath;

  public Contact() {
    this.phones = new ArrayList<>();
  }

  public Contact(String name, List<String> phones, String note) {
    this.name = name;
    this.phones = phones != null ? phones : new ArrayList<>();
    this.note = note;
  }

  public Contact(String name, List<String> phones, String note, String avatarPath) {
    this.name = name;
    this.phones = phones != null ? phones : new ArrayList<>();
    this.note = note;
    this.avatarPath = avatarPath;
  }

  public long getId() {
    return id;
  }

  public void setId(long id) {
    this.id = id;
  }

  public String getName() {
    return name;
  }

  public void setName(String name) {
    this.name = name;
  }

  public List<String> getPhones() {
    return phones;
  }

  public void setPhones(List<String> phones) {
    this.phones = phones;
  }

  public void addPhone(String phone) {
    if (this.phones == null) {
      this.phones = new ArrayList<>();
    }
    this.phones.add(phone);
  }

  public String getNote() {
    return note;
  }

  public void setNote(String note) {
    this.note = note;
  }

  public String getSortLetter() {
    return sortLetter;
  }

  public void setSortLetter(String sortLetter) {
    this.sortLetter = sortLetter;
  }

  public String getAvatarPath() {
    return avatarPath;
  }

  public void setAvatarPath(String avatarPath) {
    this.avatarPath = avatarPath;
  }

  public String getMainPhone() {
    if (phones != null && !phones.isEmpty()) {
      return phones.get(0);
    }
    return "";
  }

  public String getPhonesAsString() {
    if (phones == null || phones.isEmpty()) {
      return "";
    }
    StringBuilder sb = new StringBuilder();
    for (int i = 0; i < phones.size(); i++) {
      if (i > 0) {
        sb.append(",");
      }
      sb.append(phones.get(i));
    }
    return sb.toString();
  }

  public void setPhonesFromString(String phonesStr) {
    this.phones = new ArrayList<>();
    if (phonesStr != null && !phonesStr.isEmpty()) {
      String[] parts = phonesStr.split(",");
      for (String part : parts) {
        if (!part.trim().isEmpty()) {
          this.phones.add(part.trim());
        }
      }
    }
  }

}

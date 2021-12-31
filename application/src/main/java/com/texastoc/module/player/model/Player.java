package com.texastoc.module.player.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import java.util.Set;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.apache.commons.lang3.StringUtils;
import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.MappedCollection;

@Builder
@Getter
@Setter
@EqualsAndHashCode
@NoArgsConstructor
@AllArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class Player implements Comparable<Player> {

  @Id
  private int id;
  private String firstName;
  private String lastName;
  private String phone;
  private String email;
  private String password;
  @MappedCollection
  private Set<Role> roles;

  public String getName() {
    String name = null;

    if (firstName != null) {
      name = firstName;
      if (lastName != null) {
        name += " " + lastName;
      }
    } else if (lastName != null) {
      name = lastName;
    }

    return name == null ? "Unknown" : name;
  }

  @Override
  public int compareTo(Player other) {

    // If I don't have a first or a last
    if (firstName == null && lastName == null) {
      // then I come after other
      return 1;
    }

    // If other doesn't have a first or a last
    if (other.firstName == null && other.lastName == null) {
      // then I come before other
      return -1;
    }

    return makeFullName(this).compareTo(makeFullName(other));
  }

  private String makeFullName(Player player) {
    // Combine the first and last into a full name
    StringBuffer fullName = new StringBuffer();
    if (!StringUtils.isBlank(player.firstName)) {
      fullName.append(player.firstName);
    }
    if (!StringUtils.isBlank(player.firstName) && !StringUtils.isBlank(player.lastName)) {
      fullName.append(" ");
    }
    if (!StringUtils.isBlank(player.lastName)) {
      fullName.append(player.lastName);
    }
    return fullName.toString().toLowerCase();
  }
}

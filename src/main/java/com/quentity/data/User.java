package com.quentity.data;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.util.Set;

@Getter
@Entity
@Table(name = "application_user")
public class User {

  @Setter
  private String username;
  @Setter
  private String name;
  @Setter
  @JsonIgnore
  private String hashedPassword;
  @Setter
  @Enumerated(EnumType.STRING)
  @ElementCollection(fetch = FetchType.EAGER)
  private Set<Role> roles;
  @Setter
  @Lob
  @Column(length = 1000000)
  private byte[] profilePicture;
  @Setter
  @Id
  @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "idgenerator")
  // The initial value is to account for data.sql demo data ids
  @SequenceGenerator(name = "idgenerator", initialValue = 1000)
  private Long id;
  @Version
  private int version;

  @Override
  public int hashCode() {
    return getId().hashCode();
  }

  @Override
  public boolean equals(Object obj) {
    if (!(obj instanceof User that)) {
      return false; // null or not an AbstractEntity class
    }
    if (getId() != null) {
      return getId().equals(that.getId());
    }
    return super.equals(that);
  }
}

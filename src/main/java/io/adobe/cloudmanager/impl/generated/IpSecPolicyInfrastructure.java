/*
 * Cloud Manager API
 * This API allows access to Cloud Manager programs, pipelines, and environments by an authorized technical account created through the Adobe I/O Console. The base url for this API is https://cloudmanager.adobe.io, e.g. to get the list of programs for an organization, you would make a GET request to https://cloudmanager.adobe.io/api/programs (with the correct set of headers as described below). This swagger file can be downloaded from https://raw.githubusercontent.com/AdobeDocs/cloudmanager-api-docs/main/swagger-specs/api.yaml.
 *
 * OpenAPI spec version: 1.0.0
 * 
 *
 * NOTE: This class is auto generated by the swagger code generator program.
 * https://github.com/swagger-api/swagger-codegen.git
 * Do not edit the class manually.
 */

package io.adobe.cloudmanager.impl.generated;

import java.util.Objects;
import java.util.Arrays;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;
import io.swagger.v3.oas.annotations.media.Schema;
import java.io.Serializable;
/**
 * IpSecPolicyInfrastructure
 */



public class IpSecPolicyInfrastructure implements Serializable{
  private static final long serialVersionUID = 1L;
  /**
   * DH Group
   */
  public enum DhGroupEnum {
    DHGROUP24("DHGroup24"),
    ECP384("ECP384"),
    ECP256("ECP256"),
    DHGROUP14("DHGroup14"),
    DHGROUP2048("DHGroup2048"),
    DHGROUP2("DHGroup2"),
    DHGROUP1("DHGroup1"),
    NONE("None");

    private String value;

    DhGroupEnum(String value) {
      this.value = value;
    }
    @JsonValue
    public String getValue() {
      return value;
    }

    @Override
    public String toString() {
      return String.valueOf(value);
    }
    @JsonCreator
    public static DhGroupEnum fromValue(String input) {
      for (DhGroupEnum b : DhGroupEnum.values()) {
        if (b.value.equals(input)) {
          return b;
        }
      }
      return null;
    }

  }  @JsonProperty("dhGroup")
  private DhGroupEnum dhGroup = null;

  /**
   * IKEv2 Encryption
   */
  public enum IkeEncryptionEnum {
    AES256("AES256"),
    AES192("AES192"),
    AES128("AES128"),
    DES3("DES3"),
    DES("DES");

    private String value;

    IkeEncryptionEnum(String value) {
      this.value = value;
    }
    @JsonValue
    public String getValue() {
      return value;
    }

    @Override
    public String toString() {
      return String.valueOf(value);
    }
    @JsonCreator
    public static IkeEncryptionEnum fromValue(String input) {
      for (IkeEncryptionEnum b : IkeEncryptionEnum.values()) {
        if (b.value.equals(input)) {
          return b;
        }
      }
      return null;
    }

  }  @JsonProperty("ikeEncryption")
  private IkeEncryptionEnum ikeEncryption = null;

  /**
   * IKEv2 Integrity
   */
  public enum IkeIntegrityEnum {
    SHA384("SHA384"),
    SHA256("SHA256"),
    SHA1("SHA1"),
    MD5("MD5");

    private String value;

    IkeIntegrityEnum(String value) {
      this.value = value;
    }
    @JsonValue
    public String getValue() {
      return value;
    }

    @Override
    public String toString() {
      return String.valueOf(value);
    }
    @JsonCreator
    public static IkeIntegrityEnum fromValue(String input) {
      for (IkeIntegrityEnum b : IkeIntegrityEnum.values()) {
        if (b.value.equals(input)) {
          return b;
        }
      }
      return null;
    }

  }  @JsonProperty("ikeIntegrity")
  private IkeIntegrityEnum ikeIntegrity = null;

  /**
   * IPsec Encryption
   */
  public enum IpsecEncryptionEnum {
    GCMAES256("GCMAES256"),
    GCMAES192("GCMAES192"),
    GCMAES128("GCMAES128"),
    AES256("AES256"),
    AES192("AES192"),
    AES128("AES128"),
    DES3("DES3"),
    DES("DES"),
    NONE("None");

    private String value;

    IpsecEncryptionEnum(String value) {
      this.value = value;
    }
    @JsonValue
    public String getValue() {
      return value;
    }

    @Override
    public String toString() {
      return String.valueOf(value);
    }
    @JsonCreator
    public static IpsecEncryptionEnum fromValue(String input) {
      for (IpsecEncryptionEnum b : IpsecEncryptionEnum.values()) {
        if (b.value.equals(input)) {
          return b;
        }
      }
      return null;
    }

  }  @JsonProperty("ipsecEncryption")
  private IpsecEncryptionEnum ipsecEncryption = null;

  /**
   * IPsec Integrity
   */
  public enum IpsecIntegrityEnum {
    GCMAES256("GCMAES256"),
    GCMAES192("GCMAES192"),
    GCMAES128("GCMAES128"),
    SHA256("SHA256"),
    SHA1("SHA1"),
    MD5("MD5");

    private String value;

    IpsecIntegrityEnum(String value) {
      this.value = value;
    }
    @JsonValue
    public String getValue() {
      return value;
    }

    @Override
    public String toString() {
      return String.valueOf(value);
    }
    @JsonCreator
    public static IpsecIntegrityEnum fromValue(String input) {
      for (IpsecIntegrityEnum b : IpsecIntegrityEnum.values()) {
        if (b.value.equals(input)) {
          return b;
        }
      }
      return null;
    }

  }  @JsonProperty("ipsecIntegrity")
  private IpsecIntegrityEnum ipsecIntegrity = null;

  /**
   * PFS Group
   */
  public enum PfsGroupEnum {
    PFS24("PFS24"),
    ECP384("ECP384"),
    ECP256("ECP256"),
    PFS2048("PFS2048"),
    PFS2("PFS2"),
    PFS1("PFS1"),
    NONE("None");

    private String value;

    PfsGroupEnum(String value) {
      this.value = value;
    }
    @JsonValue
    public String getValue() {
      return value;
    }

    @Override
    public String toString() {
      return String.valueOf(value);
    }
    @JsonCreator
    public static PfsGroupEnum fromValue(String input) {
      for (PfsGroupEnum b : PfsGroupEnum.values()) {
        if (b.value.equals(input)) {
          return b;
        }
      }
      return null;
    }

  }  @JsonProperty("pfsGroup")
  private PfsGroupEnum pfsGroup = null;

  @JsonProperty("saDatasize")
  private Long saDatasize = null;

  @JsonProperty("saLifetime")
  private Integer saLifetime = null;

  public IpSecPolicyInfrastructure dhGroup(DhGroupEnum dhGroup) {
    this.dhGroup = dhGroup;
    return this;
  }

   /**
   * DH Group
   * @return dhGroup
  **/
  @Schema(description = "DH Group")
  public DhGroupEnum getDhGroup() {
    return dhGroup;
  }

  public void setDhGroup(DhGroupEnum dhGroup) {
    this.dhGroup = dhGroup;
  }

  public IpSecPolicyInfrastructure ikeEncryption(IkeEncryptionEnum ikeEncryption) {
    this.ikeEncryption = ikeEncryption;
    return this;
  }

   /**
   * IKEv2 Encryption
   * @return ikeEncryption
  **/
  @Schema(description = "IKEv2 Encryption")
  public IkeEncryptionEnum getIkeEncryption() {
    return ikeEncryption;
  }

  public void setIkeEncryption(IkeEncryptionEnum ikeEncryption) {
    this.ikeEncryption = ikeEncryption;
  }

  public IpSecPolicyInfrastructure ikeIntegrity(IkeIntegrityEnum ikeIntegrity) {
    this.ikeIntegrity = ikeIntegrity;
    return this;
  }

   /**
   * IKEv2 Integrity
   * @return ikeIntegrity
  **/
  @Schema(description = "IKEv2 Integrity")
  public IkeIntegrityEnum getIkeIntegrity() {
    return ikeIntegrity;
  }

  public void setIkeIntegrity(IkeIntegrityEnum ikeIntegrity) {
    this.ikeIntegrity = ikeIntegrity;
  }

  public IpSecPolicyInfrastructure ipsecEncryption(IpsecEncryptionEnum ipsecEncryption) {
    this.ipsecEncryption = ipsecEncryption;
    return this;
  }

   /**
   * IPsec Encryption
   * @return ipsecEncryption
  **/
  @Schema(description = "IPsec Encryption")
  public IpsecEncryptionEnum getIpsecEncryption() {
    return ipsecEncryption;
  }

  public void setIpsecEncryption(IpsecEncryptionEnum ipsecEncryption) {
    this.ipsecEncryption = ipsecEncryption;
  }

  public IpSecPolicyInfrastructure ipsecIntegrity(IpsecIntegrityEnum ipsecIntegrity) {
    this.ipsecIntegrity = ipsecIntegrity;
    return this;
  }

   /**
   * IPsec Integrity
   * @return ipsecIntegrity
  **/
  @Schema(description = "IPsec Integrity")
  public IpsecIntegrityEnum getIpsecIntegrity() {
    return ipsecIntegrity;
  }

  public void setIpsecIntegrity(IpsecIntegrityEnum ipsecIntegrity) {
    this.ipsecIntegrity = ipsecIntegrity;
  }

  public IpSecPolicyInfrastructure pfsGroup(PfsGroupEnum pfsGroup) {
    this.pfsGroup = pfsGroup;
    return this;
  }

   /**
   * PFS Group
   * @return pfsGroup
  **/
  @Schema(description = "PFS Group")
  public PfsGroupEnum getPfsGroup() {
    return pfsGroup;
  }

  public void setPfsGroup(PfsGroupEnum pfsGroup) {
    this.pfsGroup = pfsGroup;
  }

  public IpSecPolicyInfrastructure saDatasize(Long saDatasize) {
    this.saDatasize = saDatasize;
    return this;
  }

   /**
   * QM SA Lifetime Seconds min. 300
   * @return saDatasize
  **/
  @Schema(description = "QM SA Lifetime Seconds min. 300")
  public Long getSaDatasize() {
    return saDatasize;
  }

  public void setSaDatasize(Long saDatasize) {
    this.saDatasize = saDatasize;
  }

  public IpSecPolicyInfrastructure saLifetime(Integer saLifetime) {
    this.saLifetime = saLifetime;
    return this;
  }

   /**
   * QM SA Lifetime KBytes in 1024
   * @return saLifetime
  **/
  @Schema(description = "QM SA Lifetime KBytes in 1024")
  public Integer getSaLifetime() {
    return saLifetime;
  }

  public void setSaLifetime(Integer saLifetime) {
    this.saLifetime = saLifetime;
  }


  @Override
  public boolean equals(java.lang.Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    IpSecPolicyInfrastructure ipSecPolicyInfrastructure = (IpSecPolicyInfrastructure) o;
    return Objects.equals(this.dhGroup, ipSecPolicyInfrastructure.dhGroup) &&
        Objects.equals(this.ikeEncryption, ipSecPolicyInfrastructure.ikeEncryption) &&
        Objects.equals(this.ikeIntegrity, ipSecPolicyInfrastructure.ikeIntegrity) &&
        Objects.equals(this.ipsecEncryption, ipSecPolicyInfrastructure.ipsecEncryption) &&
        Objects.equals(this.ipsecIntegrity, ipSecPolicyInfrastructure.ipsecIntegrity) &&
        Objects.equals(this.pfsGroup, ipSecPolicyInfrastructure.pfsGroup) &&
        Objects.equals(this.saDatasize, ipSecPolicyInfrastructure.saDatasize) &&
        Objects.equals(this.saLifetime, ipSecPolicyInfrastructure.saLifetime);
  }

  @Override
  public int hashCode() {
    return Objects.hash(dhGroup, ikeEncryption, ikeIntegrity, ipsecEncryption, ipsecIntegrity, pfsGroup, saDatasize, saLifetime);
  }


  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder();
    sb.append("class IpSecPolicyInfrastructure {\n");
    
    sb.append("    dhGroup: ").append(toIndentedString(dhGroup)).append("\n");
    sb.append("    ikeEncryption: ").append(toIndentedString(ikeEncryption)).append("\n");
    sb.append("    ikeIntegrity: ").append(toIndentedString(ikeIntegrity)).append("\n");
    sb.append("    ipsecEncryption: ").append(toIndentedString(ipsecEncryption)).append("\n");
    sb.append("    ipsecIntegrity: ").append(toIndentedString(ipsecIntegrity)).append("\n");
    sb.append("    pfsGroup: ").append(toIndentedString(pfsGroup)).append("\n");
    sb.append("    saDatasize: ").append(toIndentedString(saDatasize)).append("\n");
    sb.append("    saLifetime: ").append(toIndentedString(saLifetime)).append("\n");
    sb.append("}");
    return sb.toString();
  }

  /**
   * Convert the given object to string with each line indented by 4 spaces
   * (except the first line).
   */
  private String toIndentedString(java.lang.Object o) {
    if (o == null) {
      return "null";
    }
    return o.toString().replace("\n", "\n    ");
  }

}

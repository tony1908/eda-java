package com.eventflow.events;

import java.util.Map;
import java.util.Objects;
public class AnonymousSchema_4 {
  private String id;
  private String reason;
  private Map<String, Object> additionalProperties;

  public String getId() { return this.id; }
  public void setId(String id) { this.id = id; }

  public String getReason() { return this.reason; }
  public void setReason(String reason) { this.reason = reason; }

  public Map<String, Object> getAdditionalProperties() { return this.additionalProperties; }
  public void setAdditionalProperties(Map<String, Object> additionalProperties) { this.additionalProperties = additionalProperties; }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    AnonymousSchema_4 self = (AnonymousSchema_4) o;
      return 
        Objects.equals(this.id, self.id) &&
        Objects.equals(this.reason, self.reason) &&
        Objects.equals(this.additionalProperties, self.additionalProperties);
  }

  @Override
  public int hashCode() {
    return Objects.hash((Object)id, (Object)reason, (Object)additionalProperties);
  }

  @Override
  public String toString() {
    return "class AnonymousSchema_4 {\n" +   
      "    id: " + toIndentedString(id) + "\n" +
      "    reason: " + toIndentedString(reason) + "\n" +
      "    additionalProperties: " + toIndentedString(additionalProperties) + "\n" +
    "}";
  }

  /**
   * Convert the given object to string with each line indented by 4 spaces
   * (except the first line).
   */
  private String toIndentedString(Object o) {
    if (o == null) {
      return "null";
    }
    return o.toString().replace("\n", "\n    ");
  }
}
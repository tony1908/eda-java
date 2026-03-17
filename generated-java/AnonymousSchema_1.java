package com.eventflow.events;

import java.util.Map;
import java.util.Objects;
public class AnonymousSchema_1 {
  private String product;
  private Double price;
  private Map<String, Object> additionalProperties;

  public String getProduct() { return this.product; }
  public void setProduct(String product) { this.product = product; }

  public Double getPrice() { return this.price; }
  public void setPrice(Double price) { this.price = price; }

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
    AnonymousSchema_1 self = (AnonymousSchema_1) o;
      return 
        Objects.equals(this.product, self.product) &&
        Objects.equals(this.price, self.price) &&
        Objects.equals(this.additionalProperties, self.additionalProperties);
  }

  @Override
  public int hashCode() {
    return Objects.hash((Object)product, (Object)price, (Object)additionalProperties);
  }

  @Override
  public String toString() {
    return "class AnonymousSchema_1 {\n" +   
      "    product: " + toIndentedString(product) + "\n" +
      "    price: " + toIndentedString(price) + "\n" +
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
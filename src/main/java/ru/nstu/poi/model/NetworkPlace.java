package ru.nstu.poi.model;

import java.util.List;
import java.util.regex.Pattern;

/** Сетевое место — редактируемое, с дополнительными полями «сайт» и «телефон». */
public class NetworkPlace extends Place implements Editable {
    public static final Pattern WEBSITE_PATTERN = Pattern.compile("^https?://[^\\s;]+$");
    public static final Pattern PHONE_PATTERN = Pattern.compile("^\\+?[0-9()\\- ]{5,20}$");

    private String website;
    private String phone;

    public NetworkPlace(long id, String name, Category category, double latitude, double longitude,
                        String website, String phone) {
        super(id, name, category, latitude, longitude);
        this.website = website == null ? "" : website;
        this.phone = phone == null ? "" : phone;
    }

    public String getWebsite() {
        return website;
    }

    public String getPhone() {
        return phone;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setCategory(Category category) {
        this.category = category;
    }

    public void setLatitude(double latitude) {
        this.latitude = latitude;
    }

    public void setLongitude(double longitude) {
        this.longitude = longitude;
    }

    public void setWebsite(String website) {
        this.website = website;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    /** Переносит все изменяемые поля из другого объекта (id не меняется). */
    public void updateFrom(NetworkPlace other) {
        setName(other.name);
        setCategory(other.category);
        setLatitude(other.latitude);
        setLongitude(other.longitude);
        setWebsite(other.website);
        setPhone(other.phone);
    }

    @Override
    public PlaceType getType() {
        return PlaceType.NETWORK;
    }

    @Override
    public List<String> validate() {
        List<String> errors = validateBaseFields();
        if (website == null || website.isBlank()) {
            errors.add("Для сетевого места нужен сайт");
        } else if (!WEBSITE_PATTERN.matcher(website).matches()) {
            errors.add("Сайт должен начинаться с http:// или https:// и не содержать пробелов");
        }
        if (phone != null && !phone.isBlank() && !PHONE_PATTERN.matcher(phone).matches()) {
            errors.add("Телефон: только цифры, пробелы, скобки, дефисы и «+» в начале (5–20 символов)");
        }
        return errors;
    }

    @Override
    public String toString() {
        return super.toString() + " сайт: " + website + ", тел.: " + phone;
    }
}

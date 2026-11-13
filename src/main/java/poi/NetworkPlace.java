package poi;

/**
 * Сетевое место: можно изменять, есть дополнительные поля сайт и телефон.
 * Только такие места создаются кнопкой «Добавить».
 */
public class NetworkPlace extends Place implements Editable {
    private String website;
    private String phone;

    public NetworkPlace(int id, String name, Category category, double latitude, double longitude,
                        String website, String phone) {
        super(id, name, category, latitude, longitude);
        this.website = website;
        this.phone = phone;
    }

    /** ЛР 4: с рейтингом и заметкой. */
    public NetworkPlace(int id, String name, Category category, double latitude, double longitude,
                        String website, String phone, double rating, String note) {
        super(id, name, category, latitude, longitude, rating, note);
        this.website = website;
        this.phone = phone;
    }

    public String getWebsite() { return website; }
    public String getPhone() { return phone; }

    // Сеттеры есть только здесь: поэтому сетевое место можно изменять
    public void setName(String name) { this.name = name; }
    public void setCategory(Category category) { this.category = category; }
    public void setLatitude(double latitude) { this.latitude = latitude; }
    public void setLongitude(double longitude) { this.longitude = longitude; }
    public void setWebsite(String website) { this.website = website; }
    public void setPhone(String phone) { this.phone = phone; }
    public void setRating(double rating) { this.rating = rating; }
    public void setNote(String note) { this.note = note; }

    @Override
    public String getType() {
        return "NETWORK";
    }

    @Override
    public String getTypeName() {
        return "Сетевое";
    }
}

package com.example.rekoro;

public class TaskItem {
    private String category;
    private String title;
    private String progress;
    private String count;
    private String description;
    private String id;
    public TaskItem() {
    }

    public TaskItem(String category, String title, String progress, String count, String description) {
        this.category = category;
        this.title = title;
        this.progress = progress;
        this.count = count;
        this.description = description;
    }
    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }
    public String getCategory() { return category; }
    public String getTitle() { return title; }
    public String getProgress() { return progress; }
    public String getCount() { return count; }
    public String getDescription() { return description; }

    public void setCategory(String category) { this.category = category; }
    public void setTitle(String title) { this.title = title; }
    public void setProgress(String progress) { this.progress = progress; }
    public void setCount(String count) { this.count = count; }
    public void setDescription(String description) { this.description = description; }
}


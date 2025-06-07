package com.example.rekoro;

public class TaskItem {
    private String category;
    private String title;
    private String progress;
    private String count;
    private String description;

    public TaskItem() {
    }

    public TaskItem(String category, String title, String progress, String count, String description) {
        this.category = category;
        this.title = title;
        this.progress = progress;
        this.count = count;
        this.description = description;
    }
    public String getCategory() { return category; }
    public String getTitle() { return title; }
    public String getProgress() { return progress; }
    public String getCount() { return count; }
    public String getDescription() { return description; }
}


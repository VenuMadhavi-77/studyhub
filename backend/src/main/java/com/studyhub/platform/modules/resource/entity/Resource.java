package com.studyhub.platform.modules.resource.entity;

import com.studyhub.platform.common.entity.BaseAuditableEntity;
import com.studyhub.platform.common.enums.ContentLanguage;
import com.studyhub.platform.common.enums.ContentLevel;
import com.studyhub.platform.common.enums.ResourceStatus;
import com.studyhub.platform.common.enums.ResourceType;
import com.studyhub.platform.common.enums.SourcePlatform;
import com.studyhub.platform.modules.auth.entity.User;
import com.studyhub.platform.modules.taxonomy.entity.Department;
import com.studyhub.platform.modules.taxonomy.entity.Subject;
import com.studyhub.platform.modules.taxonomy.entity.Tag;
import com.studyhub.platform.modules.taxonomy.entity.Topic;
import jakarta.persistence.*;
import org.hibernate.annotations.SQLDelete;
import org.hibernate.annotations.SQLRestriction;

import java.time.Instant;
import java.util.HashSet;
import java.util.Set;

@Entity
@Table(name = "resources")
@SQLDelete(sql = "UPDATE resources SET deleted = true WHERE id = ?")
@SQLRestriction("deleted = false")
public class Resource extends BaseAuditableEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String title;

    @Column(columnDefinition = "TEXT")
    private String description;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 50)
    private ResourceType type;

    @Enumerated(EnumType.STRING)
    @Column(name = "source_platform", length = 50)
    private SourcePlatform sourcePlatform = SourcePlatform.STUDYHUB;

    @Enumerated(EnumType.STRING)
    @Column(name = "level", length = 30)
    private ContentLevel level = ContentLevel.BEGINNER;

    @Enumerated(EnumType.STRING)
    @Column(name = "language", length = 30)
    private ContentLanguage language = ContentLanguage.ENGLISH;

    @Column(name = "is_free", nullable = false)
    private boolean isFree = true;

    @Column(name = "best_for", length = 255)
    private String bestFor;

    @Column(name = "verified", nullable = false)
    private boolean verified = false;

    @Column(name = "verified_by", length = 100)
    private String verifiedBy;

    @Column(name = "verified_at")
    private Instant verifiedAt;

    @Column(name = "reliability_score", nullable = false)
    private Double reliabilityScore = 0.0;

    @Column(name = "reliability_label", length = 50)
    private String reliabilityLabel = "New - not enough reviews";

    @Column(name = "file_path", length = 500)
    private String filePath;

    @Column(name = "file_mime_type", length = 100)
    private String fileMimeType;

    @Column(name = "file_size_bytes")
    private Long fileSizeBytes;

    @Column(name = "external_url", length = 1000)
    private String externalUrl;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "department_id")
    private Department department;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "subject_id", nullable = false)
    private Subject subject;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "topic_id")
    private Topic topic;

    @Column(nullable = false)
    private Integer semester;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "uploader_id", nullable = false)
    private User uploader;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private ResourceStatus status = ResourceStatus.APPROVED;

    @Column(name = "rejection_reason", columnDefinition = "TEXT")
    private String rejectionReason;

    @Column(name = "ai_summary", columnDefinition = "TEXT")
    private String aiSummary;

    @Column(name = "ai_key_points", columnDefinition = "JSON")
    private String aiKeyPoints;

    @Column(name = "ai_revision_notes", columnDefinition = "JSON")
    private String aiRevisionNotes;

    @Column(name = "avg_rating", nullable = false)
    private Double avgRating = 0.0;

    @Column(name = "rating_count", nullable = false)
    private Integer ratingCount = 0;

    @Column(name = "useful_count", nullable = false)
    private Integer usefulCount = 0;

    @Column(name = "view_count", nullable = false)
    private Integer viewCount = 0;

    @Column(name = "download_count", nullable = false)
    private Integer downloadCount = 0;

    @ManyToMany(fetch = FetchType.LAZY)
    @JoinTable(
            name = "resource_tags",
            joinColumns = @JoinColumn(name = "resource_id"),
            inverseJoinColumns = @JoinColumn(name = "tag_id")
    )
    private Set<Tag> tags = new HashSet<>();

    @Column(nullable = false)
    private boolean deleted = false;

    public Resource() {}

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public ResourceType getType() { return type; }
    public void setType(ResourceType type) { this.type = type; }

    public SourcePlatform getSourcePlatform() { return sourcePlatform; }
    public void setSourcePlatform(SourcePlatform sourcePlatform) { this.sourcePlatform = sourcePlatform; }

    public ContentLevel getLevel() { return level; }
    public void setLevel(ContentLevel level) { this.level = level; }

    public ContentLanguage getLanguage() { return language; }
    public void setLanguage(ContentLanguage language) { this.language = language; }

    public boolean isFree() { return isFree; }
    public void setFree(boolean free) { isFree = free; }

    public String getBestFor() { return bestFor; }
    public void setBestFor(String bestFor) { this.bestFor = bestFor; }

    public boolean isVerified() { return verified; }
    public void setVerified(boolean verified) { this.verified = verified; }

    public String getVerifiedBy() { return verifiedBy; }
    public void setVerifiedBy(String verifiedBy) { this.verifiedBy = verifiedBy; }

    public Instant getVerifiedAt() { return verifiedAt; }
    public void setVerifiedAt(Instant verifiedAt) { this.verifiedAt = verifiedAt; }

    public Double getReliabilityScore() { return reliabilityScore; }
    public void setReliabilityScore(Double reliabilityScore) { this.reliabilityScore = reliabilityScore; }

    public String getReliabilityLabel() { return reliabilityLabel; }
    public void setReliabilityLabel(String reliabilityLabel) { this.reliabilityLabel = reliabilityLabel; }

    public String getFilePath() { return filePath; }
    public void setFilePath(String filePath) { this.filePath = filePath; }

    public String getFileMimeType() { return fileMimeType; }
    public void setFileMimeType(String fileMimeType) { this.fileMimeType = fileMimeType; }

    public Long getFileSizeBytes() { return fileSizeBytes; }
    public void setFileSizeBytes(Long fileSizeBytes) { this.fileSizeBytes = fileSizeBytes; }

    public String getExternalUrl() { return externalUrl; }
    public void setExternalUrl(String externalUrl) { this.externalUrl = externalUrl; }

    public Department getDepartment() { return department; }
    public void setDepartment(Department department) { this.department = department; }

    public Subject getSubject() { return subject; }
    public void setSubject(Subject subject) { this.subject = subject; }

    public Topic getTopic() { return topic; }
    public void setTopic(Topic topic) { this.topic = topic; }

    public Integer getSemester() { return semester; }
    public void setSemester(Integer semester) { this.semester = semester; }

    public User getUploader() { return uploader; }
    public void setUploader(User uploader) { this.uploader = uploader; }

    public ResourceStatus getStatus() { return status; }
    public void setStatus(ResourceStatus status) { this.status = status; }

    public String getRejectionReason() { return rejectionReason; }
    public void setRejectionReason(String rejectionReason) { this.rejectionReason = rejectionReason; }

    public String getAiSummary() { return aiSummary; }
    public void setAiSummary(String aiSummary) { this.aiSummary = aiSummary; }

    public String getAiKeyPoints() { return aiKeyPoints; }
    public void setAiKeyPoints(String aiKeyPoints) { this.aiKeyPoints = aiKeyPoints; }

    public String getAiRevisionNotes() { return aiRevisionNotes; }
    public void setAiRevisionNotes(String aiRevisionNotes) { this.aiRevisionNotes = aiRevisionNotes; }

    public Double getAvgRating() { return avgRating; }
    public void setAvgRating(Double avgRating) { this.avgRating = avgRating; }

    public Integer getRatingCount() { return ratingCount; }
    public void setRatingCount(Integer ratingCount) { this.ratingCount = ratingCount; }

    public Integer getUsefulCount() { return usefulCount; }
    public void setUsefulCount(Integer usefulCount) { this.usefulCount = usefulCount; }

    public Integer getViewCount() { return viewCount; }
    public void setViewCount(Integer viewCount) { this.viewCount = viewCount; }

    public Integer getDownloadCount() { return downloadCount; }
    public void setDownloadCount(Integer downloadCount) { this.downloadCount = downloadCount; }

    public Set<Tag> getTags() { return tags; }
    public void setTags(Set<Tag> tags) { this.tags = tags; }

    public boolean isDeleted() { return deleted; }
    public void setDeleted(boolean deleted) { this.deleted = deleted; }
}

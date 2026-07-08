package com.shanhai.petplatform.infrastructure.storage;

import cn.hutool.core.lang.Dict;
import cn.hutool.core.lang.TypeReference;
import cn.hutool.json.JSONUtil;
import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import org.dromara.x.file.storage.core.FileInfo;
import org.dromara.x.file.storage.core.hash.HashInfo;

import java.io.Serializable;
import java.util.Date;
import java.util.Map;

/**
 * 文件上传记录实体 — 对应 dromara x-file-storage 的 {@link FileInfo}。
 *
 * <p>字段严格对齐 dromara 2.3.0 {@code org.dromara.x.file.storage.core.FileInfo}
 * 的真实 getter/setter（经反射确认）。Map 类字段（metadata / userMetadata 等）
 * 与 attr（hutool Dict）、fileAcl / thFileAcl（Object）、hashInfo（HashInfo）
 * 以 JSON 字符串形式存入 LONGTEXT 列，由 {@link #from(FileInfo)} / {@link #toFileInfo()}
 * 在 {@link FileInfo} 与字符串之间转换。</p>
 *
 * @author PetPlatform Team
 */
@Data
@TableName("file_info")
public class FileInfoEntity implements Serializable {

    private static final long serialVersionUID = 1L;

    /** 文件ID（dromara 生成，作为唯一主键） */
    @TableId(value = "id", type = IdType.INPUT)
    private String id;

    /** 文件访问地址 */
    private String url;

    /** 文件大小（字节） */
    private Long size;

    /** 文件名 */
    private String filename;

    /** 原始文件名 */
    private String originalFilename;

    /** 基础路径 */
    private String basePath;

    /** 路径 */
    private String path;

    /** 文件后缀 */
    private String ext;

    /** 文件 Content-Type */
    private String contentType;

    /** 存储平台（如 local / oss） */
    private String platform;

    /** 缩略图访问地址 */
    private String thUrl;

    /** 缩略图文件名 */
    private String thFilename;

    /** 缩略图大小（字节） */
    private Long thSize;

    /** 缩略图 Content-Type */
    private String thContentType;

    /** 对象ID */
    private String objectId;

    /** 对象类型 */
    private String objectType;

    /** 元数据 */
    private String metadata;

    /** 用户元数据 */
    private String userMetadata;

    /** 缩略图元数据 */
    private String thMetadata;

    /** 缩略图用户元数据 */
    private String thUserMetadata;

    /** 附加属性（hutool Dict） */
    private String attr;

    /** 文件 ACL */
    private String fileAcl;

    /** 缩略图 ACL */
    private String thFileAcl;

    /** 哈希信息 */
    private String hashInfo;

    /** 分片上传ID */
    private String uploadId;

    /** 上传状态 */
    private Integer uploadStatus;

    /** 创建时间 */
    private Date createTime;

    // ────────────────── FileInfo <-> FileInfoEntity 转换 ──────────────────

    public static FileInfoEntity from(FileInfo fileInfo) {
        if (fileInfo == null) {
            return null;
        }
        FileInfoEntity e = new FileInfoEntity();
        e.setId(fileInfo.getId());
        e.setUrl(fileInfo.getUrl());
        e.setSize(fileInfo.getSize());
        e.setFilename(fileInfo.getFilename());
        e.setOriginalFilename(fileInfo.getOriginalFilename());
        e.setBasePath(fileInfo.getBasePath());
        e.setPath(fileInfo.getPath());
        e.setExt(fileInfo.getExt());
        e.setContentType(fileInfo.getContentType());
        e.setPlatform(fileInfo.getPlatform());
        e.setThUrl(fileInfo.getThUrl());
        e.setThFilename(fileInfo.getThFilename());
        e.setThSize(fileInfo.getThSize());
        e.setThContentType(fileInfo.getThContentType());
        e.setObjectId(fileInfo.getObjectId());
        e.setObjectType(fileInfo.getObjectType());
        e.setMetadata(mapToJson(fileInfo.getMetadata()));
        e.setUserMetadata(mapToJson(fileInfo.getUserMetadata()));
        e.setThMetadata(mapToJson(fileInfo.getThMetadata()));
        e.setThUserMetadata(mapToJson(fileInfo.getThUserMetadata()));
        e.setAttr(fileInfo.getAttr() == null ? null : JSONUtil.toJsonStr(fileInfo.getAttr()));
        e.setFileAcl(objToJson(fileInfo.getFileAcl()));
        e.setThFileAcl(objToJson(fileInfo.getThFileAcl()));
        e.setHashInfo(objToJson(fileInfo.getHashInfo()));
        e.setUploadId(fileInfo.getUploadId());
        e.setUploadStatus(fileInfo.getUploadStatus());
        e.setCreateTime(fileInfo.getCreateTime());
        return e;
    }

    public FileInfo toFileInfo() {
        FileInfo fi = new FileInfo();
        fi.setId(this.id);
        fi.setUrl(this.url);
        fi.setSize(this.size);
        fi.setFilename(this.filename);
        fi.setOriginalFilename(this.originalFilename);
        fi.setBasePath(this.basePath);
        fi.setPath(this.path);
        fi.setExt(this.ext);
        fi.setContentType(this.contentType);
        fi.setPlatform(this.platform);
        fi.setThUrl(this.thUrl);
        fi.setThFilename(this.thFilename);
        fi.setThSize(this.thSize);
        fi.setThContentType(this.thContentType);
        fi.setObjectId(this.objectId);
        fi.setObjectType(this.objectType);
        fi.setMetadata(toMap(this.metadata));
        fi.setUserMetadata(toMap(this.userMetadata));
        fi.setThMetadata(toMap(this.thMetadata));
        fi.setThUserMetadata(toMap(this.thUserMetadata));
        fi.setAttr(toDict(this.attr));
        fi.setFileAcl(toObj(this.fileAcl));
        fi.setThFileAcl(toObj(this.thFileAcl));
        fi.setHashInfo(toHashInfo(this.hashInfo));
        fi.setUploadId(this.uploadId);
        fi.setUploadStatus(this.uploadStatus);
        fi.setCreateTime(this.createTime);
        return fi;
    }

    // ────────────────── JSON 转换辅助 ──────────────────

    private static String mapToJson(Map<?, ?> map) {
        return map == null ? null : JSONUtil.toJsonStr(map);
    }

    private static Map<String, String> toMap(String json) {
        if (json == null || json.isEmpty()) {
            return null;
        }
        return JSONUtil.toBean(json, new TypeReference<Map<String, String>>() {}, true);
    }

    private static Dict toDict(String json) {
        if (json == null || json.isEmpty()) {
            return null;
        }
        return Dict.parse(json);
    }

    private static String objToJson(Object obj) {
        return obj == null ? null : JSONUtil.toJsonStr(obj);
    }

    private static Object toObj(String json) {
        if (json == null || json.isEmpty()) {
            return null;
        }
        return JSONUtil.parse(json);
    }

    private static HashInfo toHashInfo(String json) {
        if (json == null || json.isEmpty()) {
            return null;
        }
        try {
            return JSONUtil.toBean(json, HashInfo.class);
        } catch (Exception e) {
            return null;
        }
    }
}

package com.tyut.implatform.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.tyut.implatform.entity.FileInfo;
import com.tyut.implatform.vo.UploadImageVO;
import org.springframework.web.multipart.MultipartFile;

public interface FileService extends IService<FileInfo> {

    String uploadFile(MultipartFile file);

    UploadImageVO uploadImage(MultipartFile file,Boolean isPermanent,Long thumbSize);


}

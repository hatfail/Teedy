package com.sismics.docs.core.dao;

import com.sismics.docs.core.constant.PermType;
import com.sismics.docs.core.dao.dto.DocumentDto;
import com.sismics.docs.core.model.jpa.Document;
import com.sismics.util.context.ThreadLocalContext;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityTransaction;
import jakarta.persistence.NoResultException;
import jakarta.persistence.TypedQuery;
import jakarta.persistence.Query;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

public class DocumentDaoTest {

    private DocumentDao documentDao;

    @Mock
    private EntityManager entityManager;

    @Mock
    private EntityTransaction transaction;

    @Mock
    private TypedQuery<Document> typedQuery;

    @Mock
    private Query query;

    @Before
    public void setUp() {
        MockitoAnnotations.initMocks(this);
        documentDao = new DocumentDao();

        // 设置模拟环境
        when(entityManager.getTransaction()).thenReturn(transaction);
        doNothing().when(transaction).begin();
        doNothing().when(transaction).commit();

        // 修复 ThreadLocalContext 用法
        ThreadLocalContext.cleanup();
        ThreadLocalContext.get().setEntityManager(entityManager);
    }

    @After
    public void tearDown() {
        ThreadLocalContext.cleanup();
    }

    @Test
    public void createDocument() {
        // 准备测试数据
        Document document = new Document();
        document.setTitle("Test Document");

        // 模拟EntityManager行为
        doNothing().when(entityManager).persist(any(Document.class));

        // 执行
        String id = documentDao.create(document, "user-id-1");

        // 验证
        Assert.assertNotNull(id);
        verify(entityManager).persist(document);
        Assert.assertNotNull(document.getUpdateDate());
    }

    @Test
    public void findAllDocuments() {
        // 准备测试数据
        List<Document> expectedDocuments = new ArrayList<>();
        expectedDocuments.add(createSampleDocument("doc-1"));
        expectedDocuments.add(createSampleDocument("doc-2"));

        // 模拟查询行为
        when(entityManager.createQuery("select d from Document d where d.deleteDate is null", Document.class))
                .thenReturn(typedQuery);
        when(typedQuery.setFirstResult(0)).thenReturn(typedQuery);
        when(typedQuery.setMaxResults(10)).thenReturn(typedQuery);
        when(typedQuery.getResultList()).thenReturn(expectedDocuments);

        // 执行
        List<Document> documents = documentDao.findAll(0, 10);

        // 验证
        Assert.assertEquals(2, documents.size());
        Assert.assertEquals("doc-1", documents.get(0).getId());
    }

    @Test
    public void findByUserIdReturnsUserDocuments() {
        // 准备测试数据
        String userId = "user-id-1";
        List<Document> expectedDocuments = new ArrayList<>();
        expectedDocuments.add(createSampleDocument("doc-1"));

        // 模拟查询行为
        when(entityManager.createQuery("select d from Document d where d.userId = :userId and d.deleteDate is null", Document.class))
                .thenReturn(typedQuery);
        when(typedQuery.setParameter("userId", userId)).thenReturn(typedQuery);
        when(typedQuery.getResultList()).thenReturn(expectedDocuments);

        // 执行
        List<Document> documents = documentDao.findByUserId(userId);

        // 验证
        Assert.assertEquals(1, documents.size());
        Assert.assertEquals("doc-1", documents.get(0).getId());
    }

    @Test
    public void getDocumentWithPermissionReturnsDocument() {
        // 准备测试数据
        String documentId = "doc-id-1";
        List<String> targetIdList = new ArrayList<>();
        targetIdList.add("target-1");

        // 模拟AclDao行为
        AclDao aclDao = mock(AclDao.class);
        when(aclDao.checkPermission(eq(documentId), eq(PermType.READ), any())).thenReturn(true);

        // 模拟查询行为
        Object[] resultRow = createSampleResultRow();
        when(entityManager.createNativeQuery(anyString())).thenReturn(query);
        when(query.setParameter("id", documentId)).thenReturn(query);
        when(query.getSingleResult()).thenReturn(resultRow);

        // 执行
        DocumentDto documentDto = documentDao.getDocument(documentId, PermType.READ, targetIdList);

        // 验证
        Assert.assertNotNull(documentDto);
        Assert.assertEquals(documentId, documentDto.getId());
        Assert.assertEquals("Test Document", documentDto.getTitle());
    }

    @Test
    public void getDocumentWithoutPermissionReturnsNull() {
        // 准备测试数据
        String documentId = "doc-id-1";
        List<String> targetIdList = new ArrayList<>();
        targetIdList.add("target-1");

        // 模拟AclDao行为 - 无权限
        AclDao aclDao = mock(AclDao.class);
        when(aclDao.checkPermission(eq(documentId), eq(PermType.READ), any())).thenReturn(false);

        // 执行
        DocumentDto documentDto = documentDao.getDocument(documentId, PermType.READ, targetIdList);

        // 验证
        Assert.assertNull(documentDto);
    }

    @Test
    public void getDocumentWithNonExistentIdReturnsNull() {
        // 准备测试数据
        String documentId = "non-existent-id";
        List<String> targetIdList = new ArrayList<>();

        // 模拟AclDao行为
        AclDao aclDao = mock(AclDao.class);
        when(aclDao.checkPermission(eq(documentId), eq(PermType.READ), any())).thenReturn(true);

        // 模拟查询行为 - 抛出NoResultException
        when(entityManager.createNativeQuery(anyString())).thenReturn(query);
        when(query.setParameter("id", documentId)).thenReturn(query);
        when(query.getSingleResult()).thenThrow(new NoResultException());

        // 执行
        DocumentDto documentDto = documentDao.getDocument(documentId, PermType.READ, targetIdList);

        // 验证
        Assert.assertNull(documentDto);
    }

    @Test
    public void deleteDocumentSetsDeleteDate() {
        // 准备测试数据
        String documentId = "doc-id-1";
        String userId = "user-id-1";
        Document document = createSampleDocument(documentId);

        // 模拟查询行为
        when(entityManager.createQuery("select d from Document d where d.id = :id and d.deleteDate is null", Document.class))
                .thenReturn(typedQuery);
        when(typedQuery.setParameter("id", documentId)).thenReturn(typedQuery);
        when(typedQuery.getSingleResult()).thenReturn(document);

        // 模拟关联查询删除
        Query mockQuery = mock(Query.class);
        when(entityManager.createQuery(anyString())).thenReturn(mockQuery);
        when(mockQuery.setParameter(anyString(), any())).thenReturn(mockQuery);
        when(mockQuery.executeUpdate()).thenReturn(1);

        // 执行
        documentDao.delete(documentId, userId);

        // 验证
        Assert.assertNotNull(document.getDeleteDate());
    }

    @Test
    public void getByIdReturnsDocument() {
        // 准备测试数据
        String documentId = "doc-id-1";
        Document expectedDocument = createSampleDocument(documentId);

        // 模拟查询行为
        when(entityManager.createQuery("select d from Document d where d.id = :id and d.deleteDate is null", Document.class))
                .thenReturn(typedQuery);
        when(typedQuery.setParameter("id", documentId)).thenReturn(typedQuery);
        when(typedQuery.getSingleResult()).thenReturn(expectedDocument);

        // 执行
        Document document = documentDao.getById(documentId);

        // 验证
        Assert.assertNotNull(document);
        Assert.assertEquals(documentId, document.getId());
    }

    @Test
    public void getByIdWithNonExistentIdReturnsNull() {
        // 准备测试数据
        String documentId = "non-existent-id";

        // 模拟查询行为
        when(entityManager.createQuery("select d from Document d where d.id = :id and d.deleteDate is null", Document.class))
                .thenReturn(typedQuery);
        when(typedQuery.setParameter("id", documentId)).thenReturn(typedQuery);
        when(typedQuery.getSingleResult()).thenThrow(new NoResultException());

        // 执行
        Document document = documentDao.getById(documentId);

        // 验证
        Assert.assertNull(document);
    }

    @Test
    public void updateDocumentModifiesProperties() {
        // 准备测试数据
        String documentId = "doc-id-1";
        String userId = "user-id-1";

        Document existingDocument = createSampleDocument(documentId);
        Document updatedDocument = new Document();
        updatedDocument.setId(documentId);
        updatedDocument.setTitle("Updated Title");
        updatedDocument.setDescription("Updated Description");

        // 模拟查询行为
        when(entityManager.createQuery("select d from Document d where d.id = :id and d.deleteDate is null", Document.class))
                .thenReturn(typedQuery);
        when(typedQuery.setParameter("id", documentId)).thenReturn(typedQuery);
        when(typedQuery.getSingleResult()).thenReturn(existingDocument);

        // 执行
        Document result = documentDao.update(updatedDocument, userId);

        // 验证
        Assert.assertEquals("Updated Title", result.getTitle());
        Assert.assertEquals("Updated Description", result.getDescription());
        Assert.assertNotNull(result.getUpdateDate());
    }

    @Test
    public void updateFileIdUpdatesFileProperty() {
        // 准备测试数据
        Document document = new Document();
        document.setId("doc-id-1");
        document.setFileId("new-file-id");

        // 模拟本机查询
        when(entityManager.createNativeQuery(anyString())).thenReturn(query);
        when(query.setParameter(anyString(), any())).thenReturn(query);
        when(query.executeUpdate()).thenReturn(1);

        // 执行
        documentDao.updateFileId(document);

        // 验证
        verify(query).executeUpdate();
    }

    @Test
    public void getDocumentCountReturnsCorrectCount() {
        // 模拟查询行为
        when(entityManager.createNativeQuery(anyString())).thenReturn(query);
        when(query.getSingleResult()).thenReturn(10L);

        // 执行
        long count = documentDao.getDocumentCount();

        // 验证
        Assert.assertEquals(10L, count);
    }

    private Document createSampleDocument(String id) {
        Document document = new Document();
        document.setId(id);
        document.setTitle("Test Document");
        document.setDescription("Test Description");
        document.setCreateDate(new Date());
        document.setUpdateDate(new Date());
        return document;
    }

    private Object[] createSampleResultRow() {
        Object[] row = new Object[18];
        row[0] = "doc-id-1"; // id
        row[1] = "Test Document"; // title
        row[2] = "Test Description"; // description
        row[3] = "Subject"; // subject
        row[4] = "Identifier"; // identifier
        row[5] = "Publisher"; // publisher
        row[6] = "Format"; // format
        row[7] = "Source"; // source
        row[8] = "Type"; // type
        row[9] = "Coverage"; // coverage
        row[10] = "Rights"; // rights
        row[11] = new java.sql.Timestamp(System.currentTimeMillis()); // create date
        row[12] = new java.sql.Timestamp(System.currentTimeMillis()); // update date
        row[13] = "eng"; // language
        row[14] = "file-id-1"; // file id
        row[15] = 1; // share count
        row[16] = 2; // file count
        row[17] = "testuser"; // creator
        return row;
    }
}
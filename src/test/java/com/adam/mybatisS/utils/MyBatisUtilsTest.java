package com.adam.mybatisS.utils;

import com.adam.mybatisS.entity.Goods;
import com.adam.mybatisS.entity.GoodsDTO;
import com.github.pagehelper.Page;
import com.github.pagehelper.PageHelper;
import junit.framework.TestCase;
import org.apache.ibatis.session.SqlSession;
import org.testng.annotations.Test;

import java.sql.Connection;
import java.util.*;

public class MyBatisUtilsTest extends TestCase {


    @Test
    public void testMyBatisUtils() throws Exception {
        SqlSession sqlSession = null;
        try {
            //一句话完成SqlSession的初始化工作
            sqlSession = MyBatisUtils.openSession();
            //执行数据库操作
            //sqlSession.insert()
            //sqlSession.update()
            Connection connection = sqlSession.getConnection();
            System.out.println(connection);
        } catch (Exception e) {
            throw e;
        } finally {
            //关闭数据连接
            MyBatisUtils.closeSession(sqlSession);
        }
    }

    @Test
    public void testSelectAll() throws Exception{
        SqlSession session = null;
        try {
            session = MyBatisUtils.openSession();
            List<Goods> list = session.selectList("goods.selectAll");
            for (Goods g:list) {
                System.out.println(g.getTitle());
                System.out.println(g.getOriginalCost());
            }
        } catch (Exception e) {
            throw e;
        } finally {
            MyBatisUtils.closeSession(session);
        }
    }
    
    @Test
    public void testSelectId() throws Exception{
        SqlSession session = null;
        try{
            session = MyBatisUtils.openSession();
            Goods goods = session.selectOne("goods.selectById", 2);
            System.out.println(goods.getTitle());
        }catch(Exception e){
            throw e;
        }finally {
            MyBatisUtils.closeSession(session);
        }
    }

    @Test
    public void testSelectByPriceRange() throws Exception{
        SqlSession session = null;
        try {
            session = MyBatisUtils.openSession();
            Map param = new HashMap();
            param.put("min",8);
            param.put("max",20);
            param.put("limit",2);
            List<Goods> list = session.selectList("goods.selectByPriceRange",param);
            for (Goods g:list){
                System.out.println(g.getTitle());
            }
        } catch (Exception e) {
            throw e;
        } finally {
            MyBatisUtils.closeSession(session);
        }
    }

    @Test
    public void selectByTitle() throws Exception{
        SqlSession session = null;
        try {
            session = MyBatisUtils.openSession();
            Map param = new HashMap();
            // param.put("title","'story book2'");
            param.put("title","'' or 1=1 or title='story book2'");
            List<Goods> list = session.selectList("goods.selectByTitle",param);
            for (Goods g:list){
                System.out.println(g.getTitle());
            }
        } catch (Exception e) {
            throw e;
        } finally {
            MyBatisUtils.closeSession(session);
        }
    }

    @Test
    public void testInsert() throws Exception{
        SqlSession session = null;
        try {
            session = MyBatisUtils.openSession();
            Goods goods = new Goods();
            goods.setTitle("测试商品");
            goods.setSubTitle("测试子标题");
            goods.setOriginalCost(200f);
            goods.setCurrentPrice(100f);
            goods.setDiscount(0.5f);
            goods.setIsFreeDelivery(1);
            goods.setCategoryId(43);
            int num = session.insert("goods.insert",goods);
            session.commit();  //提交事务数据
            System.out.println(goods.getGoodsId());
        } catch (Exception e) {
            if (session != null) {
                session.rollback();  //如果出现异常，回滚事务
            }
            throw e;
        } finally {
            if (session != null) {
                session.close();
            }
        }
    }

    @Test
    public void testUpdate() throws Exception{
        SqlSession session = null;
        try {
            session = MyBatisUtils.openSession();
            Goods goods = session.selectOne("goods.selectById",3);
            goods.setSubTitle("更新测试商品");
            int num = session.update("goods.update",goods);
            session.commit();  //提交
        } catch (Exception e) {
            if (session != null) {
                session.rollback();  //回滚
            }
            throw e;
        } finally {
            MyBatisUtils.closeSession(session);
        }
    }

    @Test
    public void testDelete() throws Exception{
        SqlSession session = null;
        try {
            session = MyBatisUtils.openSession();
            int num = session.delete("goods.delete", 3);
            session.commit();  //提交
        } catch (Exception e) {
            if (session != null) {
                session.rollback();  //回滚
            }
            throw e;
        } finally {
            MyBatisUtils.closeSession(session);
        }
    }

    @Test
    public void testDynamicSQL() throws Exception{
        SqlSession session = null;
        try {
            session = MyBatisUtils.openSession();
            Map param = new HashMap();
            param.put("categoryId", 0);
            param.put("currentPrice", 20);
            List<Goods> list = session.selectList("goods.dynamicSQL",param);
            for (Goods g: list) {
                System.out.println(g.getTitle() + " : " + g.getCategoryId() + " : " + g.getCurrentPrice());
            }
        } catch (Exception e){
            throw e;
        } finally {
            MyBatisUtils.closeSession(session);
        }
    }

    @Test
    public void testLvCache() throws Exception {
        SqlSession session = null;
        try {
            session = MyBatisUtils.openSession();
            Goods goods = session.selectOne("goods.selectById",2);
            Goods goods1 = session.selectOne("goods.selectById",2);
            System.out.println(goods.hashCode() + " : " +goods1.hashCode());  //867148091 : 867148091
        } catch (Exception e) {
            throw e;
        } finally {
            MyBatisUtils.closeSession(session);
        }
        //查看执行过程可以看出，其实只执行了一次SQL语句，由于缓存的缘故
        //从内存地址一致，可以知道只查询了一次
        try {
            session = MyBatisUtils.openSession();
            Goods goods = session.selectOne("goods.selectById",2);
            Goods goods1 = session.selectOne("goods.selectById",2);
            System.out.println(goods.hashCode() + " : " +goods1.hashCode());  //815674463 : 815674463
        } catch (Exception e) {
            throw e;
        } finally {
            MyBatisUtils.closeSession(session);
        }
        //这是另外一个SqlSession对象，因此又执行了一次查询
        //因此可以看出，一级缓存的生命周期就在一个SqlSession之内
    }

    @Test
    public void testLvCache0() throws Exception {
        SqlSession session = null;
        try {
            session = MyBatisUtils.openSession();
            Goods goods = session.selectOne("goods.selectById",2);
            session.commit();
            Goods goods1 = session.selectOne("goods.selectById",2);
            System.out.println(goods.hashCode() + " : " +goods1.hashCode());  //2051853139 : 815674463
        } catch (Exception e) {
            throw e;
        } finally {
            MyBatisUtils.closeSession(session);
        }
    }

    @Test
    public void testLvCache2() throws Exception {
        SqlSession session = null;
        try {
            session = MyBatisUtils.openSession();
            Goods goods = session.selectOne("goods.selectById",2);  //733943822
            System.out.println(goods.hashCode());
        } catch (Exception e) {
            throw e;
        } finally {
            MyBatisUtils.closeSession(session);
        }

        try {
            session = MyBatisUtils.openSession();
            Goods goods = session.selectOne("goods.selectById",2);  //733943822
            System.out.println(goods.hashCode());
        } catch (Exception e) {
            throw e;
        } finally {
            MyBatisUtils.closeSession(session);
        }
    }

    @Test
    public void testOneToMany() throws Exception {
        SqlSession session = null;
        try {
            session = MyBatisUtils.openSession();
            List<Goods> list = session.selectList("goods.selectOneToMany");
            for (Goods goods: list) {
                System.out.println(goods.getTitle() + "|" + goods.getGoodsDetails().size());
            }
        } catch (Exception e) {
            throw e;
        } finally {
            MyBatisUtils.closeSession(session);
        }
    }


    @Test
    public void testSelectPage() throws Exception {
        SqlSession session = null;
        try {
            session = MyBatisUtils.openSession();
            PageHelper.startPage(2,2);
            Page<Goods> page = (Page) session.selectList("goods.selectPage");
            System.out.println("总页数:" + page.getPages());
            System.out.println("总记录数:" + page.getTotal());
            System.out.println("开始行号:" + page.getStartRow());
            System.out.println("结束行号:" + page.getEndRow());
            System.out.println("当前页面:" + page.getPageNum());
            List<Goods> data = page.getResult();  //当前页数据
            for (Goods g: data) {
                System.out.println(g.getTitle());
            }
        } catch (Exception e) {
            throw e;
        } finally {
            MyBatisUtils.closeSession(session);
        }
    }


    @Test
    public void testBatchInsert() throws Exception {
        SqlSession session = null;
        try {
            long st = new Date().getTime();
            session = MyBatisUtils.openSession();
            List list = new ArrayList();
            for (int i = 0; i < 5; i++) {
                Goods goods = new Goods();
                goods.setTitle("测试商品");
                goods.setSubTitle("测试子标题");
                goods.setOriginalCost(200f);
                goods.setCurrentPrice(100f);
                goods.setDiscount(0.5f);
                goods.setIsFreeDelivery(1);
                goods.setCategoryId(43);
                //insert()方法返回值代表本次成功插入的记录总数
                list.add(goods);
            }
            session.insert("goods.batchInsert", list);
            session.commit();//提交事务数据
            long et = new Date().getTime();
            System.out.println("执行时间:" + (et - st) + "毫秒");
//            System.out.println(goods.getGoodsId());
        } catch (Exception e) {
            if (session != null) {
                session.rollback();//回滚事务
            }
            throw e;
        } finally {
            MyBatisUtils.closeSession(session);
        }
    }


    @Test
    public void testSelectByPriceRangeByComment() throws Exception {
        SqlSession session = null;
        try{
            session = MyBatisUtils.openSession();
            //获取映射器，GoodsDAO是接口
            //这里的GoodsDAO虽然是接口，在实际运行的时候，session会根据goodsDAO里面的配置信息来动态生成goodsDAO的实现类
            //之后直接用goodsDAO里面的方式来操作数据即可
            GoodsDAO goodsDAO = session.getMapper(GoodsDAO.class);
            List<Goods> list = goodsDAO.selectByPriceRange(5f, 51f, 20);
            System.out.println(list.size());
            for(Goods g: list){
                System.out.println("goods title: " + g.getTitle());
            }

        }catch (Exception e){
            throw e;
        } finally {
            MyBatisUtils.closeSession(session);

        }
    }


    @Test
    public void testInsertByComment() throws Exception {
        SqlSession session = null;
        try{
            session = MyBatisUtils.openSession();
            Goods goods = new Goods();
            goods.setTitle("测试商品bycomment");
            goods.setSubTitle("测试子标题bycomment");
            goods.setOriginalCost(200f);
            goods.setCurrentPrice(100f);
            goods.setDiscount(0.5f);
            goods.setIsFreeDelivery(1);
            goods.setCategoryId(43);
            GoodsDAO goodsDAO = session.getMapper(GoodsDAO.class);
            //insert()方法返回值代表本次成功插入的记录总数
            int num = goodsDAO.insert(goods);
            session.commit();//提交事务数据
            System.out.println(goods.getGoodsId());
        }catch (Exception e){
            if(session != null){
                session.rollback();//回滚事务
            }
            throw e;
        }finally {
            MyBatisUtils.closeSession(session);
        }
    }


    @Test
    public void testSelectAllByComment() throws Exception {
        SqlSession session = null;
        try{
            session = MyBatisUtils.openSession();
            GoodsDAO goodsDAO = session.getMapper(GoodsDAO.class);
            List<GoodsDTO> list = goodsDAO.selectAll();
            System.out.println(list.size());
        }catch (Exception e){
            throw e;
        } finally {
            MyBatisUtils.closeSession(session);

        }
    }

}


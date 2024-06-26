
package com.controller;

import java.io.File;
import java.math.BigDecimal;
import java.net.URL;
import java.text.SimpleDateFormat;
import com.alibaba.fastjson.JSONObject;
import java.util.*;
import org.springframework.beans.BeanUtils;
import javax.servlet.http.HttpServletRequest;
import org.springframework.web.context.ContextLoader;
import javax.servlet.ServletContext;
import com.service.TokenService;
import com.utils.*;
import java.lang.reflect.InvocationTargetException;

import com.service.DictionaryService;
import org.apache.commons.lang3.StringUtils;
import com.annotation.IgnoreAuth;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import com.baomidou.mybatisplus.mapper.EntityWrapper;
import com.baomidou.mybatisplus.mapper.Wrapper;
import com.entity.*;
import com.entity.view.*;
import com.service.*;
import com.utils.PageUtils;
import com.utils.R;
import com.alibaba.fastjson.*;

/**
 * 工作记录
 * 后端接口
 * @author
 * @email
*/
@RestController
@Controller
@RequestMapping("/jiaogongjilu")
public class JiaogongjiluController {
    private static final Logger logger = LoggerFactory.getLogger(JiaogongjiluController.class);

    private static final String TABLE_NAME = "jiaogongjilu";

    @Autowired
    private JiaogongjiluService jiaogongjiluService;


    @Autowired
    private TokenService tokenService;

    @Autowired
    private CaozuorizhiService caozuorizhiService;//操作日志
    @Autowired
    private DictionaryService dictionaryService;//字典
    @Autowired
    private MenuService menuService;//菜单
    @Autowired
    private NewsService newsService;//公告资讯
    @Autowired
    private YonghuService yonghuService;//小管理
    @Autowired
    private UsersService usersService;//管理员


    /**
    * 后端列表
    */
    @RequestMapping("/page")
    public R page(@RequestParam Map<String, Object> params, HttpServletRequest request){
        logger.debug("page方法:,,Controller:{},,params:{}",this.getClass().getName(),JSONObject.toJSONString(params));
        String role = String.valueOf(request.getSession().getAttribute("role"));
        if(false)
            return R.error(511,"永不会进入");
        else if("小管理".equals(role))
            params.put("yonghuId",request.getSession().getAttribute("userId"));
        CommonUtil.checkMap(params);
        PageUtils page = jiaogongjiluService.queryPage(params);

        //字典表数据转换
        List<JiaogongjiluView> list =(List<JiaogongjiluView>)page.getList();
        for(JiaogongjiluView c:list){
            //修改对应字典表字段
            dictionaryService.dictionaryConvert(c, request);
        }
        caozuorizhiService.insertCaozuorizhi(String.valueOf(request.getSession().getAttribute("role")),TABLE_NAME,String.valueOf(request.getSession().getAttribute("username")),"列表查询",list.toString());
        return R.ok().put("data", page);
    }

    /**
    * 后端详情
    */
    @RequestMapping("/info/{id}")
    public R info(@PathVariable("id") Long id, HttpServletRequest request){
        logger.debug("info方法:,,Controller:{},,id:{}",this.getClass().getName(),id);
        JiaogongjiluEntity jiaogongjilu = jiaogongjiluService.selectById(id);
        if(jiaogongjilu !=null){
            //entity转view
            JiaogongjiluView view = new JiaogongjiluView();
            BeanUtils.copyProperties( jiaogongjilu , view );//把实体数据重构到view中
            //修改对应字典表字段
            dictionaryService.dictionaryConvert(view, request);
    caozuorizhiService.insertCaozuorizhi(String.valueOf(request.getSession().getAttribute("role")),TABLE_NAME,String.valueOf(request.getSession().getAttribute("username")),"单条数据查看",view.toString());
            return R.ok().put("data", view);
        }else {
            return R.error(511,"查不到数据");
        }

    }

    /**
    * 后端保存
    */
    @RequestMapping("/save")
    public R save(@RequestBody JiaogongjiluEntity jiaogongjilu, HttpServletRequest request){
        logger.debug("save方法:,,Controller:{},,jiaogongjilu:{}",this.getClass().getName(),jiaogongjilu.toString());

        String role = String.valueOf(request.getSession().getAttribute("role"));
        if(false)
            return R.error(511,"永远不会进入");

        Wrapper<JiaogongjiluEntity> queryWrapper = new EntityWrapper<JiaogongjiluEntity>()
            .eq("jiaogongjilu_name", jiaogongjilu.getJiaogongjiluName())
            .eq("jiaogongjilu_types", jiaogongjilu.getJiaogongjiluTypes())
            .eq("gongzuoliang_num", jiaogongjilu.getGongzuoliangNum())
            .eq("jiaogongjilu_gongling", jiaogongjilu.getJiaogongjiluGongling())
            ;

        logger.info("sql语句:"+queryWrapper.getSqlSegment());
        JiaogongjiluEntity jiaogongjiluEntity = jiaogongjiluService.selectOne(queryWrapper);
        if(jiaogongjiluEntity==null){
            jiaogongjilu.setInsertTime(new Date());
            jiaogongjilu.setCreateTime(new Date());
            jiaogongjiluService.insert(jiaogongjilu);
            caozuorizhiService.insertCaozuorizhi(String.valueOf(request.getSession().getAttribute("role")),TABLE_NAME,String.valueOf(request.getSession().getAttribute("username")),"新增",jiaogongjilu.toString());
            return R.ok();
        }else {
            return R.error(511,"表中有相同数据");
        }
    }

    /**
    * 后端修改
    */
    @RequestMapping("/update")
    public R update(@RequestBody JiaogongjiluEntity jiaogongjilu, HttpServletRequest request) throws NoSuchFieldException, ClassNotFoundException, IllegalAccessException, InstantiationException {
        logger.debug("update方法:,,Controller:{},,jiaogongjilu:{}",this.getClass().getName(),jiaogongjilu.toString());
        JiaogongjiluEntity oldJiaogongjiluEntity = jiaogongjiluService.selectById(jiaogongjilu.getId());//查询原先数据

        String role = String.valueOf(request.getSession().getAttribute("role"));
//        if(false)
//            return R.error(511,"永远不会进入");

            jiaogongjiluService.updateById(jiaogongjilu);//根据id更新
            List<String> strings = caozuorizhiService.clazzDiff(jiaogongjilu, oldJiaogongjiluEntity, request,new String[]{"updateTime"});
            caozuorizhiService.insertCaozuorizhi(String.valueOf(request.getSession().getAttribute("role")),TABLE_NAME,String.valueOf(request.getSession().getAttribute("username")),"修改",strings.toString());
            return R.ok();
    }



    /**
    * 删除
    */
    @RequestMapping("/delete")
    public R delete(@RequestBody Integer[] ids, HttpServletRequest request){
        logger.debug("delete:,,Controller:{},,ids:{}",this.getClass().getName(),ids.toString());
        List<JiaogongjiluEntity> oldJiaogongjiluList =jiaogongjiluService.selectBatchIds(Arrays.asList(ids));//要删除的数据
        jiaogongjiluService.deleteBatchIds(Arrays.asList(ids));

        caozuorizhiService.insertCaozuorizhi(String.valueOf(request.getSession().getAttribute("role")),TABLE_NAME,String.valueOf(request.getSession().getAttribute("username")),"删除",oldJiaogongjiluList.toString());
        return R.ok();
    }


    /**
     * 批量上传
     */
    @RequestMapping("/batchInsert")
    public R save( String fileName, HttpServletRequest request){
        logger.debug("batchInsert方法:,,Controller:{},,fileName:{}",this.getClass().getName(),fileName);
        Integer yonghuId = Integer.valueOf(String.valueOf(request.getSession().getAttribute("userId")));
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        try {
            List<JiaogongjiluEntity> jiaogongjiluList = new ArrayList<>();//上传的东西
            Map<String, List<String>> seachFields= new HashMap<>();//要查询的字段
            Date date = new Date();
            int lastIndexOf = fileName.lastIndexOf(".");
            if(lastIndexOf == -1){
                return R.error(511,"该文件没有后缀");
            }else{
                String suffix = fileName.substring(lastIndexOf);
                if(!".xls".equals(suffix)){
                    return R.error(511,"只支持后缀为xls的excel文件");
                }else{
                    URL resource = this.getClass().getClassLoader().getResource("static/upload/" + fileName);//获取文件路径
                    File file = new File(resource.getFile());
                    if(!file.exists()){
                        return R.error(511,"找不到上传文件，请联系管理员");
                    }else{
                        List<List<String>> dataList = PoiUtil.poiImport(file.getPath());//读取xls文件
                        dataList.remove(0);//删除第一行，因为第一行是提示
                        for(List<String> data:dataList){
                            //循环
                            JiaogongjiluEntity jiaogongjiluEntity = new JiaogongjiluEntity();
                            jiaogongjiluEntity.setJiaogongjiluUuidNumber(data.get(0));                    //教工编号 要改的
                            jiaogongjiluEntity.setJiaogongjiluName(data.get(1));                    //教工名称 要改的
                            jiaogongjiluEntity.setJiaogongjiluTypes(Integer.valueOf(data.get(2)));   //教工类型 要改的
                            jiaogongjiluEntity.setGongzuoliangNum(Integer.valueOf(data.get(3)));   //工作量 要改的
                            jiaogongjiluEntity.setJiaogongjiluText(data.get(4));                    //
                            jiaogongjiluEntity.setJiaogongjiluGongling(Integer.valueOf(data.get(5)));   //工龄 要改的备注 要改的
                            jiaogongjiluEntity.setJiaogongjiluContent(data.get(6));//详情和图片
                            jiaogongjiluEntity.setInsertTime(date);//时间
                            jiaogongjiluEntity.setCreateTime(date);//时间
                            jiaogongjiluList.add(jiaogongjiluEntity);


                            //把要查询是否重复的字段放入map中
                                //教工编号
                                if(seachFields.containsKey("jiaogongjiluUuidNumber")){
                                    List<String> jiaogongjiluUuidNumber = seachFields.get("jiaogongjiluUuidNumber");
                                    jiaogongjiluUuidNumber.add(data.get(0));//要改的
                                }else{
                                    List<String> jiaogongjiluUuidNumber = new ArrayList<>();
                                    jiaogongjiluUuidNumber.add(data.get(0));//要改的
                                    seachFields.put("jiaogongjiluUuidNumber",jiaogongjiluUuidNumber);
                                }
                        }

                        //查询是否重复
                         //教工编号
                        List<JiaogongjiluEntity> jiaogongjiluEntities_jiaogongjiluUuidNumber = jiaogongjiluService.selectList(new EntityWrapper<JiaogongjiluEntity>().in("jiaogongjilu_uuid_number", seachFields.get("jiaogongjiluUuidNumber")));
                        if(jiaogongjiluEntities_jiaogongjiluUuidNumber.size() >0 ){
                            ArrayList<String> repeatFields = new ArrayList<>();
                            for(JiaogongjiluEntity s:jiaogongjiluEntities_jiaogongjiluUuidNumber){
                                repeatFields.add(s.getJiaogongjiluUuidNumber());
                            }
                            return R.error(511,"数据库的该表中的 [教工编号] 字段已经存在 存在数据为:"+repeatFields.toString());
                        }
                        jiaogongjiluService.insertBatch(jiaogongjiluList);
                        caozuorizhiService.insertCaozuorizhi(String.valueOf(request.getSession().getAttribute("role")),TABLE_NAME,String.valueOf(request.getSession().getAttribute("username")),"批量新增",jiaogongjiluList.toString());
                        return R.ok();
                    }
                }
            }
        }catch (Exception e){
            e.printStackTrace();
            return R.error(511,"批量插入数据异常，请联系管理员");
        }
    }




}


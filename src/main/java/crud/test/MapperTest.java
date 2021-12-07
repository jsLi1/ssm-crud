package crud.test;

import crud.bean.Department;
import crud.bean.Employee;
import crud.dao.DepartmentMapper;
import crud.dao.EmployeeMapper;
import org.apache.ibatis.session.SqlSession;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import java.util.UUID;

/**
 * 测试dao层的工作
 * 推荐spring的项目就可以使用spring的单元测试，可以自动注入我们需要的组件
 * 导入springttest模块
 */
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations = {"classpath:applicationContext.xml"})
public class MapperTest {
    @Autowired
    DepartmentMapper departmentMapper;
    @Autowired
    EmployeeMapper employeeMapper;

    @Autowired
    SqlSession sqlsession;
    /**
     * 测试DepartmentMapper
      */
    @Test
     public void testCRUD(){
//      //1.创建spring的ioc容器
//        ApplicationContext ioc=new ClassPathXmlApplicationContext("applicationContext.xml");
//        //从容器中获取mapper
//        DepartmentMapper bean = ioc.getBean(DepartmentMapper.class);
//        System.out.println(departmentMapper);
//
//        //1.插入几个部门
//        departmentMapper.insertSelective(new Department(null,"开发部"));
//        departmentMapper.insertSelective(new Department(null,"测试部"));
         //生成员工数据，测试员工拆入
        //employeeMapper.insertSelective(new Employee(null,"Jery","M","Jerry@163.com",1));
         //批量插入多个员工
        EmployeeMapper mapper = sqlsession.getMapper(EmployeeMapper.class);
        for (int i=0;i<1000;i++){
            String uid = UUID.randomUUID().toString().substring(0, 5)+i;
            mapper.insertSelective(new Employee(null,uid,"M",uid+"@163.com",1));
            System.out.println("批量成功");
        }
    }
}

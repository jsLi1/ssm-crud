package crud.controller;

import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import crud.bean.Employee;
import crud.bean.Msg;
import crud.service.EmployeeService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import javax.validation.Valid;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 处理员工crud请求
 */
@Controller
public class EmployeeController {
    @Autowired
    EmployeeService employeeService;

    @ResponseBody
    @RequestMapping(value = "/emp/{ids}",method = RequestMethod.DELETE)
    public Msg deleteEmpById(@PathVariable("ids") String ids){
        //批量删除
        if(ids.contains("-")){
            List<Integer> del_ids = new ArrayList<>();
            String[] str_ids = ids.split("-");
            //组装id的集合
            for (String string : str_ids) {
                del_ids.add(Integer.parseInt(string));
            }
            employeeService.deleteBatch(del_ids);
        }else{
            Integer id = Integer.parseInt(ids);
            employeeService.deleteEmp(id);
        }
        return Msg.success();
    }


    /**
     * 如果直接发送ajax=PUT形式的请求
     * 封装的数据
     * Employee
     * [empId=1014, empName=null, gender=null, email=null, dId=null]
     *
     * 问题：
     * 请求体中有数据；
     * 但是Employee对象封装不上；
     * update tbl_emp  where emp_id = 1014;
     *
     * 原因：
     * Tomcat：
     * 		1、将请求体中的数据，封装一个map。
     * 		2、request.getParameter("empName")就会从这个map中取值。
     * 		3、SpringMVC封装POJO对象的时候。
     * 				会把POJO中每个属性的值，request.getParamter("email");
     * AJAX发送PUT请求引发的血案：
     * 		PUT请求，请求体中的数据，request.getParameter("empName")拿不到
     * 		Tomcat一看是PUT不会封装请求体中的数据为map，只有POST形式的请求才封装请求体为map
     * org.apache.catalina.connector.Request--parseParameters() (3111);
     *
     * protected String parseBodyMethods = "POST";
     * if( !getConnector().isParseBodyMethod(getMethod()) ) {
     success = true;
     return;
     }
     *
     *
     * 解决方案；
     * 我们要能支持直接发送PUT之类的请求还要封装请求体中的数据
     * 1、配置上HttpPutFormContentFilter；
     * 2、他的作用；将请求体中的数据解析包装成一个map。
     * 3、request被重新包装，request.getParameter()被重写，就会从自己封装的map中取数据
     * 员工更新方法
     * @param employee
     * @return
     */
    @ResponseBody
    @RequestMapping(value="/emp/{empId}",method=RequestMethod.PUT)
    public Msg saveEmp(Employee employee, HttpServletRequest request){
        System.out.println("请求体中的值："+request.getParameter("gender"));
        System.out.println("将要更新的员工数据："+employee);
        employeeService.updateEmp(employee);
        return Msg.success()	;
    }



    @RequestMapping(value = "/emp/{id}",method =RequestMethod.GET )
    @ResponseBody
    public Msg getEmp(@PathVariable("id") Integer id){
       Employee employee= employeeService.getEmp(id);
        return Msg.success().add("emp",employee);
    }



      @RequestMapping(value = "/checkuser")
    @ResponseBody
    public Msg checkuse(@RequestParam("empName") String empName){
          //先判断用户名是否合法
          String regx = "(^[a-zA-Z0-9_-]{6,16}$)|(^[\u2E80-\u9FFF]{2,5})";
          if(!empName.matches(regx)){
              return Msg.fail().add("va_msg","后端校验 用户名必须6-16位数字和字母的组合");
          }
          boolean b=employeeService.checkUser(empName);
          if(b){
              return Msg.success();
          }else{
              return Msg.fail().add("va_msg","用户名不可用");
          }
    }
    //员工保存
    @RequestMapping(value = "/emp",method = RequestMethod.POST)
    @ResponseBody
    public Msg saveEmp(@Valid Employee employee, BindingResult result){
        if(result.hasErrors()){
            Map<String,Object> map=new HashMap<>();
            List<FieldError> fieldErrors = result.getFieldErrors();
            for(FieldError fieldError:fieldErrors){
                System.out.println("错误的字段名："+fieldError.getField());
                System.out.println("错误信息："+fieldError.getDefaultMessage());
          map.put(fieldError.getField(),fieldError.getDefaultMessage());
            }
            return Msg.fail().add("errorFields",map);
        }else{
            employeeService.saveEmp(employee);
            return Msg.success();
        }
    }
    /**
     * 担任如jakson包
     * @param pn
     * @return
     */
      @RequestMapping("/emps")
     @ResponseBody
     public Msg getEmpsWithJson(@RequestParam(value = "pn",defaultValue = "1")Integer pn){
         //引入pagehelper分页插件,在查询之前只需要调用,传入页码,以及每页大小
         PageHelper.startPage(pn,5);
         //startpage后面紧跟的这个查询就是一个分页查询
         List<Employee> emps= employeeService.getAll();
         //使用pageinfo包装查询后的结果，只需将pageinfo交给页面就行了
         //封装了详细的分页信息，包括了我们查询出来的数据,传入连续显示的页数
         PageInfo page=new PageInfo(emps,5);
         return Msg.success().add("pageInfo",page);
     }
  //@RequestMapping("/emps")
    public String getEmps(@RequestParam(value = "pn",defaultValue = "1")Integer pn, Model model){
      //引入pagehelper分页插件,在查询之前只需要调用,传入页码,以及每页大小
      PageHelper.startPage(pn,5);
      //startpage后面紧跟的这个查询就是一个分页查询
   List<Employee> emps= employeeService.getAll();
   //使用pageinfo包装查询后的结果，只需将pageinfo交给页面就行了
      //封装了详细的分页信息，包括了我们查询出来的数据,传入连续显示的页数
      PageInfo page=new PageInfo(emps,5);
      model.addAttribute("pageInfo",page);
      return "list";
    }

}

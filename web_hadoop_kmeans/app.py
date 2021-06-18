import pymysql as pymysql
from flask import Flask, render_template, request
from itertools import chain
from collections import Counter
import json

app = Flask(__name__)

db = pymysql.connect("localhost", "root", "root123456", "Kmeans")
#name--->下拉框城市数据
def dropdown(sql1):
    cursor =db.cursor()
    cursor.execute(sql1)
    results1 =cursor.fetchall()
    #二维元组转一维列表
    cities = list(chain.from_iterable(results1)) #'name'
    return cities

#name_lng_lat---->{'name':[lng,lat]……}----->转成json格式
def sample1(sql2,sql3):
    # 使用cursor()方法获取操作游标
    cursor1 = db.cursor() #用于获取坐标
    cursor2 = db.cursor() #用于获取城市名
    # 执行sql语句
    cursor1.execute(sql2)
    cursor2.execute(sql3)

    results2 = cursor1.fetchall()
    results3 = cursor2.fetchall()

    results2 = list(results2)
    results3 = list(chain.from_iterable(results3))#二维转一维
    for i in results2:
        results2[results2.index(i)] = list(i)
    name_lng_lat=dict(zip(results3,results2))#'name':[lng,lat]
    json_name_lng_lat=json.dumps(name_lng_lat,ensure_ascii=False)
    return json_name_lng_lat

#name_value------>[{'name':'city_name','value':number}]----->转成json和格式
def sample2(sql3):
    cursor=db.cursor()
    cursor.execute(sql3)
    results = cursor.fetchall()
    results = list(chain.from_iterable(results))
    name_value = Counter(results) #({'name':number})
    name_value = dict(name_value) #{'name':number}

    list_dict= []
    for key,value in name_value.items():
        mydict = {}
        mydict["name"] = key
        mydict["value"] = value
        list_dict.append(mydict) #[{'name':'city_name','value':number}]
    json_name_value = json.dumps(list_dict,ensure_ascii=False)#转换成json格式[{"name":"city_name","value":number}]

    # with open('data/name_value.json','w') as f:
    #     f.write(json_name_value)
    #     f.close()

    return json_name_value

#k_lng_lat------>[[lng,lat]……]
def sample3(sql4):
    cursor = db.cursor()
    cursor.execute(sql4)
    results = cursor.fetchall()
    results = list(chain.from_iterable(results))
    k_lng_lat = [results[i:i+2] for i in range(0,len(results),2)]
    return k_lng_lat


@app.route('/',methods=['GET','POST'])
def index():
    get_city=request.form.get('cities')
    #get_city = '北京市'
    sql1 = "select start_name \
            from k_cities \
          "
    sql2 = "select end_lng,end_lat\
            from complex  \
            where start_name='{}'".format(get_city)

    sql3 = "select end_name\
            from complex  \
            where start_name='{}'".format(get_city)

    sql4 = "select k1_lng,k1_lat,k2_lng,k2_lat,k3_lng,k3_lat \
            from k_cities \
            where start_name='{}'".format(get_city)
    cities = dropdown(sql1)
    name_lng_lat=sample1(sql2,sql3)
    name_value = sample2(sql3)
    k_lng_lat = sample3(sql4)
    return render_template("index.html",cities=cities,name_lng_lat=name_lng_lat,name_value=name_value,k_lng_lat=k_lng_lat,get_city=get_city)




if __name__ == '__main__':
    app.run(debug=True)

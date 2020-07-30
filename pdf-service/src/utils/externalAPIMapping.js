import get from "lodash/get";
import axios from "axios";
import { httpRequest } from "../api/api";
import {
  findAndUpdateLocalisation,
  getDateInRequiredFormat,
  getValue
} from "./commons";
import logger from "../config/logger";

import envVariables from "../EnvironmentVariables";
const egovFileHost = envVariables.EGOV_FILESTORE_SERVICE_HOST;
const egovHost = envVariables.EGOV_HOST;
let egovHostUrl = envVariables.EGOV_HOST_LOGO_URL;
/**
 *
 * @param {*} key -name of the key used to identify module configs. Provided request URL
 * @param {*} req -current module object, picked from request body
 * @param {*} dataconfig - data config
 * @param {*} variableTovalueMap -map used for filling values by template engine 'mustache'
 * @param {*} localisationMap -Map to store localisation key, value pair
 * @param {*} requestInfo -request info from request body
 */

export const externalAPIMapping = async function(
  key,
  req,
  dataconfig,
  variableTovalueMap,
  localisationMap,
  requestInfo,
  localisationModuleList
) {
  var jp = require("jsonpath");
  var objectOfExternalAPI = getValue(
    jp.query(dataconfig, "$.DataConfigs.mappings.*.mappings.*.externalAPI.*"),
    [],
    "$.DataConfigs.mappings.*.mappings.*.externalAPI.*"
  );
  var externalAPIArray = objectOfExternalAPI.map(item => {
    return {
      uri: item.path,
      queryParams: item.queryParam,
      jPath: item.responseMapping,
      requesttype: item.requesttype || "POST",
      variable: "",
      val: ""
    };
  });
  for (let i = 0; i < externalAPIArray.length; i++) {
    var temp1 = "";
    var temp2 = "";
    var flag = 0;
    //to convert queryparam and uri into properURI

    //for PT module
    if (key == "pt-receipt") {
      for (let j = 0; j < externalAPIArray[i].queryParams.length; j++) {
        if (externalAPIArray[i].queryParams[j] == "$") {
          flag = 1;
        }
        if (
          externalAPIArray[i].queryParams[j] == "," ||
          externalAPIArray[i].queryParams[j] == ":"
        ) {
          if (flag == 1) {
            temp2 = temp1;
            var temp3 = getValue(jp.query(req, temp1), "NA", temp1);
            externalAPIArray[i].queryParams = externalAPIArray[
              i
            ].queryParams.replace(temp2, temp3);

            j = 0;
            flag = 0;
            temp1 = "";
            temp2 = "";
          }
        }

        if (flag == 1) {
          temp1 += externalAPIArray[i].queryParams[j];
        }
        if (j == externalAPIArray[i].queryParams.length - 1 && flag == 1) {
          temp2 = temp1;
          var temp3 = getValue(jp.query(req, temp1), "NA", temp1);

          externalAPIArray[i].queryParams = externalAPIArray[
            i
          ].queryParams.replace(temp2, temp3);

          flag = 0;
          temp1 = "";
          temp2 = "";
        }
      }
    }
    //for other modules
    else {
      for (let j = 0; j < externalAPIArray[i].queryParams.length; j++) {
        if (externalAPIArray[i].queryParams[j] == "{") {
          externalAPIArray[i].queryParams = externalAPIArray[
            i
          ].queryParams.replace("{","");
      }
      

        if (externalAPIArray[i].queryParams[j] == "$") {
          flag = 1;
        }
        if (
          externalAPIArray[i].queryParams[j] == "," ||
          externalAPIArray[i].queryParams[j] == "}"
        ) {
          if (flag == 1) {
            temp2 = temp1;
            
            var temp3 = getValue(jp.query(req, temp1), "NA", temp1);
            externalAPIArray[i].queryParams = externalAPIArray[
              i
            ].queryParams.replace(temp2, temp3);

            j = 0;
            flag = 0;
            temp1 = "";
            temp2 = "";
          }
          if (externalAPIArray[i].queryParams[j] == "}") {
            externalAPIArray[i].queryParams = externalAPIArray[
              i
            ].queryParams.replace("}","");
        }
          
        }
        if (flag == 1) {
          temp1 += externalAPIArray[i].queryParams[j];
        }
        if (j == externalAPIArray[i].queryParams.length - 1 && flag == 1) {
          temp2 = temp1;
          var temp3 = getValue(jp.query(req, temp1), "NA", temp1);

          externalAPIArray[i].queryParams = externalAPIArray[
            i
          ].queryParams.replace(temp2, temp3);

          flag = 0;
          temp1 = "";
          temp2 = "";
        }
      }
    }
    externalAPIArray[i].queryParams = externalAPIArray[i].queryParams.replace(
      /,/g,
      "&"
    );
    let headers = {
      "content-type": "application/json;charset=UTF-8",
      accept: "application/json, text/plain, */*"
    };
    var res;
    if (externalAPIArray[i].requesttype == "POST") {
      res = await httpRequest(
        externalAPIArray[i].uri + "?" + externalAPIArray[i].queryParams,
        { RequestInfo: requestInfo },
        headers
      );
      if(key == "tradelicense-appl-receipt" || key == "tradelicense-receipt" || key == "consolidatedreceipt" || key == "tlcertificate")
      {
        //console.warn("calling in---"+JSON.stringify(res));
        if(null!=res.MdmsRes)
        {
        if(null!=res.MdmsRes.tenant.tenants)
        {
          if(res.MdmsRes.tenant.tenants.length>0)
          {
        
        let tempObj = res.MdmsRes.tenant.tenants[0].logoIdPdf;
        res.MdmsRes.tenant.tenants[0].logoIdPdf = egovHostUrl.concat(tempObj);
        //console.warn("respo is tempObj--->>"+res.MdmsRes.tenant.tenants[0].logoIdPdf);
          }
        }
      }
    }
    } else {
      var apires = await axios.get(
        externalAPIArray[i].uri + "?" + externalAPIArray[i].queryParams,
        {
          responseType: "application/json"
        }
      );
      res = apires.data;
      console.log("default resp-->>"+JSON.stringify(res));
      console.log("file store url-->>"+egovFileHost);
      if(null!=res)
      {
        if(null!=res.fileStoreIds)
        {
          if(res.fileStoreIds.length>0)
          {
              let tempUrl = res.fileStoreIds[0].url;
              console.log("temp url old--"+tempUrl);
              res.fileStoreIds[0].url = tempUrl.replace(egovHost,egovFileHost);
              console.log("temp new --"+res.fileStoreIds[0].url);
          }
        }
      }
    }
    //putting required data from external API call in format config

    for (let j = 0; j < externalAPIArray[i].jPath.length; j++) {
      let replaceValue = getValue(
        jp.query(res, externalAPIArray[i].jPath[j].value),
        "NA",
        externalAPIArray[i].jPath[j].value
      );   
      let loc = externalAPIArray[i].jPath[j].localisation;
      if (externalAPIArray[i].jPath[j].type == "image") {
        // default empty image
        var imageData =
          "data:image/jpeg;base64,iVBORw0KGgoAAAANSUhEUgAAAAEAAAABCAQAAAC1HAwCAAAAC0lEQVR42mP8Xw8AAoMBgDTD2qgAAAAASUVORK5CYII=";
          if (replaceValue != "NA") {
          try {
            var len = replaceValue[0].split(",").length;
            var response = await axios.get(
              replaceValue[0].split(",")[len - 1],
              {
                responseType: "arraybuffer"
              }
            );
            imageData =
              "data:" +
              response.headers["content-type"] +
              ";base64," +
              Buffer.from(response.data).toString("base64");
          } catch (error) {
            logger.error(error.stack || error);
            throw {
              message: `error while loading image from: ${replaceValue[0]}`
            };
          }
        }
        variableTovalueMap[externalAPIArray[i].jPath[j].variable] = imageData;
      } else if (externalAPIArray[i].jPath[j].type == "date") {
        let myDate = new Date(replaceValue[0]);
        if (isNaN(myDate) || replaceValue[0] === 0) {
          variableTovalueMap[externalAPIArray[i].jPath[j].variable] = "NA";
        } else {
          replaceValue = getDateInRequiredFormat(replaceValue[0]);
          variableTovalueMap[
            externalAPIArray[i].jPath[j].variable
          ] = replaceValue;
        }
      }
      else if (externalAPIArray[i].jPath[j].type == "array"){

        let arrayOfOwnerObject = [];
      // let ownerObject = JSON.parse(JSON.stringify(get(formatconfig, directArr[i].jPath + "[0]", [])));
      let { format = {}, value = [], variable } = externalAPIArray[i].jPath[j];
      let { scema = [] } = format;
      let val= getValue(jp.query(res, value ), "NA", value);
      console.warn("value--->"+value+"data is--"+JSON.stringify(val)+ "length-->"+val.length+ "type--"+typeof(val));
        if(typeof val !== "string") // if the var is not present and the value is NA
        {
      //taking values about owner from request body
      for (let l = 0; l < val.length; l++) {
        // var x = 1;
        let ownerObject = {};
        for (let k = 0; k < scema.length; k++) {
          let fieldValue = get(val[l], scema[k].value, "NA");
          if (scema[k].type == "date") {
            let myDate = new Date(fieldValue);
            if (isNaN(myDate) || fieldValue === 0) {
              ownerObject[scema[k].key] = "NA";
            } else {
              let replaceValue = getDateInRequiredFormat(fieldValue);
              // set(formatconfig,externalAPIArray[i].jPath[j].variable,replaceValue);
              ownerObject[scema[k].key] = replaceValue;
            }
          } else {
            if (
              fieldValue !== "NA" &&
              scema[k].localisation &&
              scema[k].localisation.required
            ) {
              let loc = scema[k].localisation;
              fieldValue = await findAndUpdateLocalisation(
                requestInfo,
                localisationMap,
                loc.prefix,
                fieldValue,
                loc.module,
                localisationModuleList,
                loc.isCategoryRequired,
                loc.isMainTypeRequired,
                loc.isSubTypeRequired,
                loc.delimiter
              );
            }
            else
            if(fieldValue === "NA" &&
            scema[k].localisation &&
            scema[k].localisation.required &&
            !(scema[k].localisation.isCategoryRequired))
            {
              fieldValue="";
            }
            ownerObject[scema[k].variable] = fieldValue;
            
          }
          // set(ownerObject[x], "text", get(val[j], scema[k].key, ""));
          // x += 2;
        }
        arrayOfOwnerObject.push(ownerObject);
        
      }
    }
     else{
      console.log("setting empty")
      arrayOfOwnerObject = " ";
     }
     //console.log("arrayOfOwnerObject--"+JSON.stringify(arrayOfOwnerObject));
      variableTovalueMap[variable] = arrayOfOwnerObject;
      //console.log("\nvariableTovalueMap[externalAPIArray[i].jPath.variable]--->\n"+JSON.stringify(variableTovalueMap[externalAPIArray[i].jPath.variable]));

      } 
      
      else {
        if (
          replaceValue !== "NA" &&
          externalAPIArray[i].jPath[j].localisation &&
          externalAPIArray[i].jPath[j].localisation.required &&
          externalAPIArray[i].jPath[j].localisation.prefix
        )
          variableTovalueMap[
            externalAPIArray[i].jPath[j].variable
          ] = await findAndUpdateLocalisation(
            requestInfo,
            localisationMap,
            loc.prefix,
            replaceValue,
            loc.module,
            localisationModuleList,
            loc.isCategoryRequired,
            loc.isMainTypeRequired,
            loc.isSubTypeRequired,
            loc.delimiter
          );
        else
          variableTovalueMap[
            externalAPIArray[i].jPath[j].variable
          ] = replaceValue;
        if (externalAPIArray[i].jPath[j].isUpperCaseRequired) {
          let currentValue =
            variableTovalueMap[externalAPIArray[i].jPath[j].variable];
          if (typeof currentValue == "object" && currentValue.length > 0)
            currentValue = currentValue[0];

          variableTovalueMap[
            externalAPIArray[i].jPath[j].variable
          ] = currentValue.toUpperCase();
        }
      }
    }
  }
};

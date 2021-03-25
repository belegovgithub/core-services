import { httpRequest } from "../api/api";
import logger from "../config/logger";
import envVariables from "../EnvironmentVariables";
import get from "lodash/get";
var moment = require("moment-timezone");
var jp = require("jsonpath");
let datetimezone = envVariables.DATE_TIMEZONE;
let egovLocHost = envVariables.EGOV_LOCALISATION_HOST;
let defaultLocale = envVariables.DEFAULT_LOCALISATION_LOCALE;
let defaultTenant = envVariables.DEFAULT_LOCALISATION_TENANT;
export const getTransformedLocale = (label) => {
  return label.toUpperCase().replace(/[.:-\s\/]/g, "_");
};

/**
 * This function returns localisation label from keys based on needs
 * This function does optimisation to fetch one module localisation values only once
 * @param {*} requestInfo - requestinfo from client
 * @param {*} localisationMap - localisation map containing localisation key,label fetched till now
 * @param {*} prefix - prefix to be added before key before fetching localisation ex:-"MODULE_NAME_MASTER_NAME"
 * @param {*} key - key to fetch localisation
 * @param {*} moduleName - "module name for fetching localisation"
 * @param {*} localisationModuleList - "list of modules for which localisation was already fetched"
 * @param {*} isCategoryRequired - ex:- "GOODS_RETAIL_TST-1" = get localisation for "GOODS"
 * @param {*} isMainTypeRequired  - ex:- "GOODS_RETAIL_TST-1" = get localisation for "RETAIL"
 * @param {*} isSubTypeRequired  - - ex:- "GOODS_RETAIL_TST-1" = get localisation for "GOODS_RETAIL_TST-1"
 */
export const findAndUpdateLocalisation = async (
  requestInfo,
  localisationMap,
  prefix,
  key,
  moduleName,
  localisationModuleList,
  isCategoryRequired,
  isMainTypeRequired,
  isSubTypeRequired,
  delimiter = " / ",
  statetenantid
) => {
  let keyArray = [];
  let localisedLabels = [];
  let isArray = false;
  let locale = requestInfo.msgId;
  if (null != locale) {
    locale = locale.split("|");
    locale = locale.length > 1 ? locale[1] : defaultLocale;
  } else {
    locale = defaultLocale;
  }

  if(!statetenantid)
  {
   statetenantid = get(
    requestInfo,
    "userInfo.tenantId",
    defaultTenant
  ).split(".")[0];
  }

  if (key == null) {
    return key;
  } else if (typeof key == "string" || typeof key == "number") {
    keyArray.push(key);
  } else {
    keyArray = key;
    isArray = true;
  }

  if (!localisationModuleList.includes(moduleName)) {
    var res = await httpRequest(
      `${egovLocHost}/localization/messages/v1/_search?locale=${locale}&tenantId=${statetenantid}&module=${moduleName}`,
      { RequestInfo: requestInfo }
    );
    res.messages.map((item) => {
      localisationMap[item.code] = item.message;
    });
    localisationModuleList.push(moduleName);
  }
  keyArray.map((item) => {
    let labelFromKey = "";

    // append main category in the beginning
    if (isCategoryRequired) {
      labelFromKey = getLocalisationLabel(
        item.split(".")[0],
        localisationMap,
        prefix
      );
    }

    if (isMainTypeRequired) {
      if (isCategoryRequired) labelFromKey = `${labelFromKey}${delimiter}`;
      labelFromKey = getLocalisationLabel(
        item.split(".")[1],
        localisationMap,
        prefix
      );
    }

    if (isSubTypeRequired) {
      if (isMainTypeRequired || isCategoryRequired)
        labelFromKey = `${labelFromKey}${delimiter}`;
      labelFromKey = `${labelFromKey}${getLocalisationLabel(
        item,
        localisationMap,
        prefix
      )}`;
    }

    if (!isCategoryRequired && !isMainTypeRequired && !isSubTypeRequired) {
      labelFromKey = getLocalisationLabel(item, localisationMap, prefix);
    }

    localisedLabels.push(labelFromKey === "" ? item : labelFromKey);
  });
  if (isArray) {
    return localisedLabels;
  }
  return localisedLabels[0];
};

const getLocalisationLabel = (key, localisationMap, prefix) => {
  if (prefix != undefined && prefix != "") {
    key = `${prefix}_${key}`;
  }
  key = getTransformedLocale(key);

  if (localisationMap[key]) {
    return localisationMap[key];
  } else {
    logger.error(`no localisation value found for key ${key}`);
    return key;
  }
};

export const getDefaultFontStyle = (locale = "en_IN") => {
  let fontStyle = 'Cambay';
  if (locale != undefined && locale != "") {
    switch (locale) {
      case 'kn_IN':
        fontStyle = 'kannada'
        break;
      case 'ml_IN':
        fontStyle = 'malyalam'
        break;
      case 'ta_IN':
          fontStyle = 'tamil'
          break;
      case 'te_IN':
            fontStyle = 'telugu'
            break;
      case 'hi_IN':
      case 'en_IN':
          fontStyle = 'Cambay'
          break;
      default:
         fontStyle = 'Cambay';
  }
}
return fontStyle;  
}

export const getDateInRequiredFormat = (et, dateformat = "DD/MM/YYYY") => {
  //console.log("getDateInRequiredFormat--",et);
  if (!et) return "NA";
  // var date = new Date(Math.round(Number(et)));
  return moment(et).tz(datetimezone).format(dateformat);
};

export const getDateInWordFormat = (et, dateformat = "DD/MM/YYYY") => {
  //console.log("getDateInRequiredFormat--",et);
  if (!et) return "NA";
  // var date = new Date(Math.round(Number(et)));
  let parsedDate =  moment(et).tz(datetimezone).format(dateformat);
  let check = moment(parsedDate, dateformat);
  let splitDate = parsedDate.split("/");
  let dateStr = getNumberInWords(splitDate[0]);
  let yearStr = getAmountInWords(splitDate[2]);
  let finalStr = parsedDate + " " +dateStr + " " + check.format('MMMM') + " " + yearStr; 
  console.log("date value---",finalStr)
  return finalStr;
};

export const getAmountInWords = (value) => {
  let a = ['','One ','Two ','Three ','Four ', 'Five ','Six ','Seven ','Eight ','Nine ','Ten ','Eleven ','Twelve ','Thirteen ','Fourteen ','Fifteen ','Sixteen ','Seventeen ','Eighteen ','Nineteen '];
  let b = ['', '', 'Twenty','Thirty','Forty','Fifty', 'Sixty','Seventy','Eighty','Ninety'];
  let finalStr = '';

  if ((value = value.toString()).length > 9) 
  finalStr = "NA";
  let n = ('000000000' + value).substr(-9).match(/^(\d{2})(\d{2})(\d{2})(\d{1})(\d{2})$/);
  if (!n) finalStr = "NA"; 
  let str = '';
  str += (n[1] != 0) ? (a[Number(n[1])] || b[n[1][0]] + ' ' + a[n[1][1]]) + 'Crore ' : '';
  str += (n[2] != 0) ? (a[Number(n[2])] || b[n[2][0]] + ' ' + a[n[2][1]]) + 'Lakh ' : '';
  str += (n[3] != 0) ? (a[Number(n[3])] || b[n[3][0]] + ' ' + a[n[3][1]]) + 'Thousand ' : '';
  str += (n[4] != 0) ? (a[Number(n[4])] || b[n[4][0]] + ' ' + a[n[4][1]]) + 'Hundred ' : '';
  str += (n[5] != 0) ? ((str != '') ? 'and ' : '') + (a[Number(n[5])] || b[n[5][0]] + ' ' + a[n[5][1]])  : '';
  finalStr = str;
  return finalStr;
};

 const getNumberInWords = (value) => {
  let a = ['','First ','Second ','Third ','Fourth ', 'Fifth ','Sixth ','Seventh ','Eighth ','Nineth ','Tenth ','Eleventh ','Twelveth ','Thirteenth ','Fourteenth ','Fifteenth ','Sixteenth ','Seventeenth ','Eighteenth ','Nineteenth '];
  let b = ['', '', 'Twenty','Thirty','Forty','Fifty', 'Sixty','Seventy','Eighty','Ninety'];
  let finalStr = '';

  if ((value = value.toString()).length > 9) 
  finalStr = "NA";
  let n = ('000000000' + value).substr(-9).match(/^(\d{2})(\d{2})(\d{2})(\d{1})(\d{2})$/);
  if (!n) finalStr = "NA"; 
  let str = '';
  str += (n[1] != 0) ? (a[Number(n[1])] || b[n[1][0]] + ' ' + a[n[1][1]]) + 'Crore ' : '';
  str += (n[2] != 0) ? (a[Number(n[2])] || b[n[2][0]] + ' ' + a[n[2][1]]) + 'Lakh ' : '';
  str += (n[3] != 0) ? (a[Number(n[3])] || b[n[3][0]] + ' ' + a[n[3][1]]) + 'Thousand ' : '';
  str += (n[4] != 0) ? (a[Number(n[4])] || b[n[4][0]] + ' ' + a[n[4][1]]) + 'Hundred ' : '';
  str += (n[5] != 0) ? ((str != '') ? 'and ' : '') + (a[Number(n[5])] || b[n[5][0]] + ' ' + a[n[5][1]])  : '';
  finalStr = str;
  return finalStr;
};


export const getDateInNewFinYear = (et, dateformat = "DD/MM/YYYY") => {
  if (!et) return "NA";
  var date = new Date(Math.round(Number(et)));
  var formattedDate =
    "01" + "/" + "04"+ "/" + date.getFullYear();
  return formattedDate;
};

export const getCurrentFinancialYear = (et, dateformat = "DD/MM/YYYY") => {  
    var financial_year = "";
    var today = new Date(Math.round(Number(et)));
    if ((today.getMonth() + 1) <= 3) {
        financial_year = (today.getFullYear() - 1) + "-" + today.getFullYear()
    } else {
        financial_year = today.getFullYear() + "-" + (today.getFullYear() + 1)
    }
    return financial_year;
}

/**
 *
 * @param {*} value - values to be checked
 * @param {*} defaultValue - default value
 * @param {*} path  - jsonpath from where the value was fetched
 */
export const getValue = (value, defaultValue, path) => {
  if (
    value == undefined ||
    value == null ||
    value.length === 0 ||
    (value.length === 1 && (value[0] === null || value[0] === ""))
  ) {
    // logger.error(`no value found for path: ${path}`);
    return defaultValue;
  } else return value;
};

export const convertFooterStringtoFunctionIfExist = (footer) => { 
  if (footer != undefined) {
    footer = eval(footer);
  }
  //console.log("footer next---",footer);
  return footer;
};

/*export const convertBackgroundImagetoFunctionIfExist = (background , dataconfig) => {
  //console.log("footer value---",background);
  var objectOfDirectMapping = jp.query(
    dataconfig,
    "$.DataConfigs.mappings.*.mappings.*.direct.*"
  );
  if (background != undefined) {
    background = eval(background);
  }
 
  console.log("background next---",background);
  let sample = background();
  console.log("sample---",sample.image);
  //console.log("objectOfDirectMapping---",objectOfDirectMapping);
  var res = objectOfDirectMapping.filter(item => item.variable == sample.image)
  console.log("res---",res[0].url);
  
  var attribute = get(objectOfDirectMapping[0].variable, sample.image, "NA");
  console.log("sample---",attribute);
  return background;
};*/

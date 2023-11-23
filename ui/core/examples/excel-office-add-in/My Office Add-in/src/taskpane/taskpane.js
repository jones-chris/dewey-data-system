const COLUMN_HEADERS_MAPPING = ['A', 'B', 'C', 'D', 'E', 'F', 'G', 'H', 'I', 'J', 'K', 'L', 'M', 'N', 'O', 'P', 'Q', 'R',
    'S', 'T', 'U', 'V', 'W', 'X', 'Y', 'Z', 'AA', 'AB', 'AC', 'AD', 'AE', 'AF', 'AG', 'AH', 'AI', 'AK', 'AL', 'AM', 'AN',
    'AO', 'AP', 'AQ', 'AR', 'AS', 'AT', 'AU', 'AV', 'AW', 'AX', 'AY', 'AZ'
];

document.getElementById('loadIFrame')
    .addEventListener(
        'click',
        () => {
            const qbUrl = document.getElementById('qbUrl').value;
            document.getElementById('qbIframe').src = qbUrl;
        }
    )

window.onmessage = async (event) => {
    console.log('In the event handler!');
    console.log(event);

    try {
        await Excel.run(async (context) => {
            // Check if there is a Data sheet.
            let sheets = context.workbook.worksheets;
            sheets.load('items/name');

            await context.sync();

            let dataSheets = sheets.items.filter(sheet => sheet.name.toLowerCase() === 'data');

            // If not, then create a Data sheet and copy/paste qb columns and data.
            if (dataSheets.length === 0) {
                setRangeValues(context, sheets, event.data, null);
                await context.sync();
            }
            else {
            // If so, delete all data and copy/paste qb columns and data.
                let dataSheet = dataSheets[0]
                let range = dataSheet.getRange('A1:AZ1000000');
                range.clear();

                await context.sync();

                setRangeValues(context, sheets, event.data, dataSheet);

                await context.sync();
            }

            // todo:  Loop through all pivot tables and refresh if source is Data worksheet.
//            const pivotTables = context.workbook.pivotTables;
//            pivotTables.load('items');
//            await context.sync();
//
//            pivotTables.items.forEach(async (pivotTable) => {
//                const dataSourceType = pivotTable.getDataSourceType();
//                const dataSourceString = pivotTable.getDataSourceString();
//
//                await context.sync();
//
//                console.log(dataSourceType);
//                console.log(dataSourceString);
//            });

            // todo: loop through all pivot charts and refresh if source is Data worksheet.

            await context.sync();
        })
    } catch (error) {
        console.error(error);
    }
}

const setRangeValues = async (excelContext, sheets, data, dataWorksheet = null) => {
    if (dataWorksheet === null) {
        let dataWorksheet = sheets.add('data');
        dataWorksheet.load('name');

        await excelContext.sync();
    }

    // Ending Excel worksheet column name.
    let endingWorksheetColumnName = COLUMN_HEADERS_MAPPING[data.columns.length - 1];
    let endingWorksheetRowNumber = data.data.length + 1;

    // Write column names.
    let columnRange = dataWorksheet.getRange(`A1:${endingWorksheetColumnName}1`);
    columnRange.values = [data.columns];  // Wrap this in an array because Range#values must be a 2D array.

    // Write data.
    let dataRange = dataWorksheet.getRange(`A2:${endingWorksheetColumnName}${endingWorksheetRowNumber}`);
    dataRange.values = data.data;
};

Office.onReady((info) => {
  if (info.host === Office.HostType.Excel) {
    document.getElementById("sideload-msg").style.display = "none";
    document.getElementById("app-body").style.display = "flex";
    document.getElementById("run").onclick = run;
  }
});

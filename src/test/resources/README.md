For the developer's sanity's sake, each API uses a different program.

#### Program to Tests Breakdown

Most of the tests will use a specific program to help delinate context. These help track those contexts.

| API |Program ID|
| :--- | -----------: |
|Environments|2|
|Pipelines|3|
|Executions|4|

#### Program Details:

|Program | Test State|
| :--- | ---: |
| 2 | Delete - Bad Request|
| 3 | Delete - Success |

#### Environments
|Program | Environment | Test State|
| :--- | --- | ---: |
| 1 | | Not Found |
| 3 | | Empty List |
| 2 | | List Success |
| 2 | 3 | Delete Fails - Bad Request |
| 2 | 12 | Delete Fails - Not Found <br/> Get Variable Fails <br/> Download Log fails|
| 2 | 1 | Delete Success <br/> Set Variables Success <br> Download Logs success |
| 2 | 2 | Missing Variables Link <br/> Missing Logs Link |
| 2 | 3 | No Developer Console <br/> Get Variables Not Found <br/> Set Variables Bad Request <br/> Download Logs 404 |
| 2 | 4 | Variable List Empty <br/> Set Variable Empty List | 

#### Pipeline Details
|Program | Pipeline | Test State|
| :--- | --- | ---: |
| 1 | | Pipelines Not Found|
| 2 | | List Empty|
| 3 | | List Success |
| 3 | 1 | Start Success <br/> Patch Success <br/> Delete Success <br/> Variable successes |
| 3 | 2 | Start Fails - Running <br/> Missing Variables Link |
| 3 | 3 | Start Fails - Not Found <br/> Get Variables not found <br/> Set Variables Bad Request |
| 3 | 4 | Update Fails - Not Allowed <br/> Delete Fails - Bad Request <br/> Variable List Empty <br/> Set Variable Empty List |
| 3 | 10 | Pipeline Missing |

#### Execution Details:

| Pipeline | Execution| Test State | 
| :--- | :---: | ---: |
| 1 |  |  Not Running (404) |
| 2 | 1 | Current Running <br/> Cancel Current Running |
| 3 | 1 | Specific Running <br/> Cancel Specific Running <br> Cancel via Execution |
| 3 | 2 | Cancel/Advance - Code Quality <br/> Logs 404 <br/> Log Redirect Empty |
| 3 | 3 | Cancel/Advance - Code Quality Invalid State |
| 3 | 4 | Cancel/Advance - Approval Waiting <br/> Log Download Successes |
| 3 | 5 | Cancel - Deploy Waiting <br/> Missing Execution Step 'devDeploy' |
| 3 | 6 | Cancel - Deploy Invalid State |
| 3 | 7 | Advance - Build Running | 
| 4 | 1 | No Steps | 
| 5 | 1 | No Active Step |
| 10 |  | Execution Missing (404) |

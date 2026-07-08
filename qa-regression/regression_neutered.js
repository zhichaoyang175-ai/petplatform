/**
 * 回归测试：编辑宠物 neutered 字段类型收敛（修复 HttpMessageNotReadableException）
 *
 * 背景：
 *   后端 PetUpdateRequest.neutered 为 Integer（0/1），PetVO.neutered 被转成 Boolean。
 *   原 EditPetView 预填写法 `d.neutered ?? 0` 会把 Boolean 原样发回 Integer 字段，
 *   触发 "Cannot deserialize value of type java.lang.Integer from Boolean value" 400 错误。
 *   修复后使用 `const conv = (v) => v ? 1 : 0;` 统一收敛为 number 0/1。
 *
 * 本脚本复现 conv 逻辑，覆盖 5 种输入，验证输出恒为 number 0/1。
 */

'use strict';

// 复现前端修复逻辑（与 EditPetView.vue:55 一致）
const conv = (v) => v ? 1 : 0;

// 转换后字段类型必须为 number（否则仍可能触发后端反序列化问题）
const isStrictNumber = (v) => typeof v === 'number' && Number.isFinite(v);

const cases = [
  { desc: 'Boolean true (PetVO 返回 true)',     input: true,  expected: 1 },
  { desc: 'Boolean false (PetVO 返回 false)',    input: false, expected: 0 },
  { desc: 'null (无绝育字段)',                    input: null,  expected: 0 },
  { desc: 'Integer 1 (兼容旧契约)',              input: 1,     expected: 1 },
  { desc: 'Integer 0 (兼容旧契约)',              input: 0,     expected: 0 },
];

console.log('=== 编辑宠物 neutered 转换逻辑回归测试 ===');
console.log('转换函数: const conv = (v) => v ? 1 : 0;');
console.log('');
console.log(
  pad('输入描述', 34),
  pad('输入值', 12),
  pad('期望', 8),
  pad('实际', 8),
  pad('类型', 10),
  '结果'
);

let pass = 0;
let fail = 0;

for (const c of cases) {
  const actual = conv(c.input);
  const valueOk = actual === c.expected;
  const typeOk = isStrictNumber(actual);
  const ok = valueOk && typeOk;
  ok ? pass++ : fail++;

  console.log(
    pad(c.desc, 34),
    pad(String(c.input), 12),
    pad(String(c.expected), 8),
    pad(String(actual), 8),
    pad(typeof actual, 10),
    ok ? 'PASS' : 'FAIL'
  );

  if (!ok) {
    console.log(`   -> 失败细节: 值正确=${valueOk}, 类型为number=${typeOk}`);
  }
}

console.log('');
console.log(`汇总: 通过 ${pass} / 失败 ${fail} / 共 ${cases.length}`);

const allPass = fail === 0;
console.log(`REGRESSION RESULT: ${allPass ? 'PASS (全部通过)' : 'FAIL (存在失败用例)'}`);

// 退出码：全通过为 0，否则为 1（便于 CI 判定）
process.exit(allPass ? 0 : 1);

function pad(s, n) {
  s = String(s);
  return s.length >= n ? s : s + ' '.repeat(n - s.length);
}
